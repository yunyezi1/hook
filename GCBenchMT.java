import android.util.ArrayMap;
import android.util.ArraySet;

import android.util.Pair;
import android.util.SparseArray;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GCBenchMT {
    public static Method getDeclaredMethod(Object object, String methodName, Class<?> ... parameterTypes) {
        Method method = null ;

        for(Class<?> clazz = object.getClass() ; clazz != Object.class ; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes) ;
                return method ;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了

            }
        }

        return null;
    }

    static class MonitorTest implements Runnable {
        Object lockObject;
        Object[] lockObjects;
        HashSet<Object> hashSetTest = new HashSet<Object>();
        HashMap<Integer, Object> hashMapTest = new HashMap<Integer, Object>();
        TreeSet<Object> treeSetTest = new TreeSet<Object>();
        TreeMap<Integer, Object> treeMapTest = new TreeMap<Integer, Object>();
        ArrayList<Object> arrayListTest = new ArrayList<Object>();
        SparseArray<Object> sparseArrayTest = new SparseArray<Object>();
        ArraySet<Object> arraySetTest = new ArraySet<Object>();
        ArrayMap<Integer, Object> arrayMapTest = new ArrayMap<Integer, Object>();
        LinkedList<Object> linkedListTest = new LinkedList<Object>();
        LinkedHashMap<Integer, Object> linkedHashMapTest = new LinkedHashMap<Integer, Object>();
        Lock lock;
        int type;
        int subtype;
        int loops;
        public static int condition = 0;
        static int value = 0;

        public MonitorTest(Object lockObject, int type, int subtype, int loops) {
            this.lockObject = lockObject;
            this.type = type;
            this.loops = loops;
            this.subtype = subtype;
        }

        public MonitorTest(Object[] lockObjects, int type, int subtype, int loops) {
            this.lockObjects = lockObjects;
            this.type = type;
            this.subtype = subtype;
            this.loops = loops;
            for (Integer i = 0; i < lockObjects.length; i++) {
                hashSetTest.add(lockObjects[i]);
                hashMapTest.put(i, lockObjects[i]);
                treeSetTest.add(lockObjects[i]);
                treeMapTest.put(i, lockObjects[i]);
                arrayListTest.add(lockObjects[i]);
                sparseArrayTest.put(i, lockObjects[i]);
                arraySetTest.add(lockObjects[i]);
                arrayMapTest.put(i, lockObjects[i]);
                linkedListTest.add(lockObjects[i]);
                linkedHashMapTest.put(i, lockObjects[i]);
            }
        }

        public MonitorTest(Lock lock, int type, int subtype, int loops) {
            this.lock = lock;
            this.type = type;
            this.subtype = subtype;
            this.loops = loops;
        }

        public void syncFunc(int cnt) {
            synchronized (lockObject) {
                if (cnt == 0) return;
                value++;
                syncFunc(cnt - 1);
            }
        }

        @Override
        public void run() {
            if (this.type == 1)
                RecurSyncTest();
            else if (this.type == 2)
                LoopSyncTest(subtype);
            else if (this.type == 3)
                MultiThreadTest();
            else if (this.type == 4)
                MultiObjectTest();
            else if (this.type == 5)
                WaitSyncTest();
            else if (this.type == 6)
                LockSyncTest();
            else if (this.type == 7)
                SetSyncTest(subtype);
            else if (this.type == 8)
                SetSearchTest(subtype);
        }

        private void RecurSyncTest() {
            int i = 0, j = 0;
            int x = (1 << 10) - 4;
            long start, end;
            start = System.currentTimeMillis();
            for (i = 0; i < loops; i++) {
                syncFunc(x);
            }
            end = System.currentTimeMillis();
            System.out.println("recursion sync Executes: " + (end - start) + "ms");
            System.out.println(value);
        }

        private void LoopSyncTest(int subtype) {
            long start, end;
            int j = 0;
            start = System.currentTimeMillis();
            if (subtype == 1) {
                int i = 0;
                int x = (1 << 12) - 4;
                for (i = 0; i < loops; i++) {
                    for (j = 0; j < x; j++) {
                        synchronized (lockObject) {
                            //System.out.println(value);
                            value++;
                        }
                    }
                }
            }else if(subtype == 2){
                int i = 0;
                int x = (1 << 12) - 4;
                for (i = 0; i < loops; i++) {
                    for (int k = 0; k < x; k++) {
                        j = (j + i) % (k + 5);
                    }
                }
            }else if (subtype == 3) {
                for (int i = 0; i < loops * 100000; i++)
                    j = (j + i) % (i + 5);
            } else if (subtype == 4) {
                synchronized (lockObject) {
                    for (int i = 0; i < loops * 100000; i++) {
                        j = (j + i) % (i + 5);
                    }
                }
            }
            end = System.currentTimeMillis();
            System.out.println("loop sync Executes: " + (end - start) + "ms; j = " + j);
        }

        private void MultiThreadTest() {
            int i = 0;
            long start, end;
            start = System.currentTimeMillis();
            for (i = 0; i < loops; i++) {
                Random rand = new Random();
                int n = rand.nextInt(50) + 50;
                try {
                    Thread.sleep(n);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (lockObject) {
                    //System.out.println(value);
                    value++;
                }
            }
            end = System.currentTimeMillis();
            System.out.println("Thread " + Thread.currentThread().getName() + ": Executes: " + (end - start) + "ms");
        }

        private void MultiObjectTest() {
            int i = 0, j = 0;
            long start, end;
            int x = (1 << 10) - 4;
            start = System.currentTimeMillis();
            for (i = 0; i < loops; i++) {
                for (j = 0; j < x; j++) {
                    for (Object obj : lockObjects) {
                        synchronized (obj) {
                            //System.out.println(value);
                            value++;
                        }
                    }
                }
            }
            end = System.currentTimeMillis();
            System.out.println("multi object Executes: " + (end - start) + "ms");
        }

        private void WaitSyncTest() {
            int i = 0;
            long start, end;
            start = System.currentTimeMillis();
            for (i = 0; i < loops; i++) {
                synchronized (lockObject) {
                    try {
                        while (condition == 0)
                            lockObject.wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    condition = 0;
                    //System.out.println(value);
                    value++;
                }
            }
            end = System.currentTimeMillis();
            System.out.println("wait sync Executes: " + (end - start) + "ms");
        }

        private void LockSyncTest() {
            int i = 0, j = 0;
            int x = (1 << 12) - 4;
            long start, end;
            start = System.currentTimeMillis();
            for (i = 0; i < loops; i++) {
                for (j = 0; j < x; j++) {
                    lock.lock();
                    value++;
                    lock.unlock();
                }
            }
            end = System.currentTimeMillis();
            System.out.println("lock object Executes: " + (end - start) + "ms");
        }

        private void SetSyncTest(int subtype) {
            long start, end;
            int j = 10, salt = 30;
            Object[] testComps = new Object[]{};
            start = System.nanoTime();

            end = System.nanoTime();
            System.out.println("set sync Executes: " + (end - start) + "us; j = " + j);
        }

        private void SetSearchTest(int subtype) {
            long start = 0, end = 0, total = 0;
            int j = 10, salt = 99;
            Integer[] testCases = new Integer[]{6,66,666,6666,66666,99999,88888};
            ArrayList<ArrayList<Integer>> testCasess = new ArrayList<ArrayList<Integer>>(){{
                    add(new ArrayList<Integer>(){{add(1);add(2);add(3);add(4);add(5);add(6);}});
                    add(new ArrayList<Integer>(){{add(11);add(22);add(33);add(44);add(55);add(66);}});
                    add(new ArrayList<Integer>(){{add(111);add(222);add(333);add(444);add(555);add(666);}});
                    add(new ArrayList<Integer>(){{add(1111);add(2222);add(3333);add(4444);add(5555);add(6666);}});
                    add(new ArrayList<Integer>(){{add(11111);add(22222);add(33333);add(44444);add(55555);add(66666);}});
                    add(new ArrayList<Integer>(){{add(91111);add(92222);add(93333);add(94444);add(95555);add(99999);}});
                    add(new ArrayList<Integer>(){{add(-1);add(0);add(88888);}});
            }};
            Pair<Class,Object>[] testComponets = new Pair[]{
                    new Pair(HashSet.class, hashSetTest),
                    new Pair(HashMap.class,hashMapTest),
                    new Pair(TreeSet.class,treeSetTest),
                    new Pair(TreeMap.class,treeMapTest),
                    new Pair(ArrayList.class,arrayListTest),
                    new Pair(ArraySet.class,arraySetTest),
                    new Pair(ArrayMap.class,arrayMapTest),
                    new Pair(SparseArray.class,sparseArrayTest),
                    new Pair(LinkedList.class,linkedListTest),
                    new Pair(LinkedHashMap.class,linkedHashMapTest)
            };
            Pair<String,Class<?>[]>[] testMethods = new Pair[]{
                    new Pair("0-clone",new Class<?>[]{}),
                    new Pair("0-size",new Class<?>[]{}),
                    new Pair("0-isEmpty",new Class<?>[]{}),
                    new Pair("1-equals",new Class<?>[]{Object.class}),//单参数
                    new Pair("1-contains",new Class<?>[]{Object.class}),
                    new Pair("3-containsAll",new Class<?>[]{Collection.class}),
                    new Pair("1-containsKey",new Class<?>[]{Object.class}),
                    new Pair("1-containsValue",new Class<?>[]{Object.class}),
                    new Pair("1-indexOfKey",new Class<?>[]{int.class}),
                    new Pair("1-indexOfValue",new Class<?>[]{Object.class}),
                    new Pair("1-indexOf",new Class<?>[]{Object.class}),
                    new Pair("1-keyAt",new Class<?>[]{int.class}),
                    new Pair("1-valueAt",new Class<?>[]{int.class}),
                    new Pair("1-get",new Class<?>[]{int.class}),
                    new Pair("1-get",new Class<?>[]{Object.class}),
                    new Pair("2-put",new Class<?>[]{Integer.class,Object.class}),//双参数
                    new Pair("2-setValueAt",new Class<?>[]{int.class,Object.class}),
                    new Pair("2-append",new Class<?>[]{int.class,Object.class}),
                    new Pair("4-putAll",new Class<?>[]{Map.class}),
                    new Pair("5-entrySet",new Class<?>[]{}),//双迭代器
                    new Pair("6-keySet",new Class<?>[]{}),
                    new Pair("6-values",new Class<?>[]{}),
                    new Pair("7-iterator",new Class<?>[]{}),//迭代器
                    new Pair("1-remove",new Class<?>[]{Object.class}),
                    new Pair("1-delete",new Class<?>[]{int.class}),
                    new Pair("1-removeAt",new Class<?>[]{int.class}),
                    new Pair("3-removeAtRange",new Class<?>[]{Collection.class}),
                    new Pair("3-removeAll",new Class<?>[]{Collection.class}),
                    new Pair("1-add",new Class<?>[]{Object.class}),
                    new Pair("3-addAll",new Class<?>[]{Collection.class}),//集合参数
                    new Pair("0-clear",new Class<?>[]{}),//无参数
            };
            for(Pair<Class,Object> com : testComponets){
                System.out.println("Test Container " + com.first.toString() + " Begin ============<<<<<<");
                for(Pair<String,Class<?>[]> md : testMethods){
                    try {
                        if(j <= 0)
                            j = 10;
                        j = (j + salt) % j;
                        String[] pattern = md.first.split("-");
                        int type = Integer.parseInt(pattern[0]);
                        String name = pattern[1];
                        Method method = GCBenchMT.getDeclaredMethod(com.second, name, md.second);
                        if(method == null)
                            continue;
                        method.setAccessible(true);
                        switch (type) {
                            case 0://无参调用
                                start = System.nanoTime();
                                method.invoke(com.second);
                                end = System.nanoTime();
                                total = end - start;
                                break;
                            case 1://1参调用
                                start = System.nanoTime();
                                for (Integer i : testCases)
                                    method.invoke(com.second, i);
                                end = System.nanoTime();
                                total = (end - start) / testCases.length;
                                break;
                            case 2://2参调用
                                start = System.nanoTime();
                                for (Integer i : testCases)
                                    method.invoke(com.second, i, i);
                                end = System.nanoTime();
                                total = (end - start) / testCases.length;
                                break;
                            case 3://集合调用
                                start = System.nanoTime();
                                for (ArrayList<Integer> i : testCasess)
                                    method.invoke(com.second,  i);
                                end = System.nanoTime();
                                total = (end - start) / testCases.length;
                                break;
                            case 4://集合写入
                                int count = testCases.length;
                                Map<Integer,ArrayList<Integer>> map = new HashMap<>();
                                for(j=0;j < count;j++)
                                    map.put(testCases[j],testCasess.get(j));
                                start = System.nanoTime();
                                method.invoke(com.second, map);
                                end = System.nanoTime();
                                total = (end - start) / testCases.length;
                                break;
                            case 5://entrySet
                                start = System.nanoTime();
                                Set<Map.Entry<Object,Object>> set = (Set<Map.Entry<Object,Object>>)method.invoke(com.second);
                                for (Map.Entry<Object,Object> e : set)
                                    e.setValue(new Integer(3));
                                end = System.nanoTime();
                                total = end - start;
                                break;
                            case 6://set
                                start = System.nanoTime();
                                Collection<Object> keys = (Collection<Object>)method.invoke(com.second);
                                for(Object key : keys)
                                    j += Integer.parseInt(key.toString());
                                end = System.nanoTime();
                                total = end - start;
                                break;
                            case 7://iterator
                                start = System.nanoTime();
                                Iterator iter = (Iterator)method.invoke(com.second);
                                while(iter.hasNext()){
                                    j += Integer.parseInt(iter.next().toString());
                                }
                                end = System.nanoTime();
                                total = end - start;
                                break;
                            default:
                                break;
                        }
                        System.out.println("set method " + name + " Executes: " + total + "ns; j = " + j);
                    }catch (Exception e){
                        System.out.println("error name:" + md.first);
                        e.printStackTrace();
                        continue;
                    }
                }
                System.out.println("Test Container " + com.first.toString() + " End ============>>>>>>>");
            }
        }
    }

    public static void testRescursionSync(int subtype, int loops) {
        System.out.println("testCase1 : recursion sync begin ------------<<<");
        Object lockObject = new Object();
        Thread t = new Thread(new MonitorTest(lockObject, 1, subtype, loops), "testCase1");
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("testCase1 : recursion sync end ------------>>>");
    }

    public static void testLoopSync(int subtype, int loops) {
        System.out.println("testCase2 : loop sync begin ------------<<<");
        Object lockObject = new Object();
        Thread t = new Thread(new MonitorTest(lockObject, 2, subtype, loops), "testCase2");
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("testCase2 : loop sync end ------------>>>");
    }

    public static void testMultiThread(int subtype, int loops) {
        System.out.println("testCase3 : multi thread sync begin ------------<<<");
        Object lockObject = new Object();
        Thread [] threads = new Thread[20];
        for(int i=0;i<20;i++){
            threads[i] = new Thread(new MonitorTest(lockObject, 3, subtype, loops), "testCase3_" + i);
            threads[i].start();
        }
        for(int i=0;i<20;i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("testCase3 : multi thread sync end ------------>>>");
    }

    public static void testMultiObj(int subtype, int loops) {
        System.out.println("testCase4 : multi object sync begin ------------<<<");
        Object[] lockObjects = new Object[10];
        for(int i=0;i<10;i++)
            lockObjects[i] = new Integer(i);
        Thread t = new Thread(new MonitorTest(lockObjects, 4, subtype, loops), "testCase4");
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("testCase4 : multi object sync end ------------>>>");
    }

    public static void testWaitSync(int subtype, int loops) {
        System.out.println("testCase5 : wait sync begin ------------<<<");
        Object lockObject = new Object();
        Thread t1 = new Thread(new MonitorTest(lockObject, 5, subtype, loops), "testCase5_1");
        Thread t2 = new Thread(new MonitorTest(lockObject, 5, subtype, loops), "testCase5_2");
        t1.start();
        t2.start();
        try {
            for(int i=0;i<loops * 2;i++){
                Thread.sleep(50);
                synchronized(lockObject){
                    MonitorTest.condition = 1;
                    lockObject.notifyAll();
                }
            }
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("testCase5 : wait sync end ------------>>>");
    }

    public static void testLockSync(int subtype, int loops) {
        System.out.println("testCase6 : lock sync begin ------------<<<");
        Lock lockObject = new ReentrantLock();
        Thread t = new Thread(new MonitorTest(lockObject, 6, subtype, loops), "testCase6");
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("testCase6 : lock sync end ------------>>>");
    }

    public static void testSetSync(int subtype, int loops) {
        System.out.println("testCase7 : set sync begin ------------<<<");
        Object[] lockObjects = new Object[1000];
        for(int i=0;i<1000;i++)
            lockObjects[i] = new Integer(i);
        Thread t = new Thread(new MonitorTest(lockObjects, 7, subtype, loops), "testCase7");
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("testCase7 : set sync end ------------>>>");
    }

    public static void testSetSearch(int subtype, int loops) {
        System.out.println("testCase8 : set search begin ------------<<<");
        Object[] lockObjects = new Object[100000];
        for(int i=0;i<100000;i++)
            lockObjects[i] = new Integer(i);
        MonitorTest tester = new MonitorTest(lockObjects, 8, subtype, loops);
        tester.SetSearchTest(subtype);
        Thread t = new Thread(new MonitorTest(lockObjects, 8, subtype, loops), "testCase8");
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("testCase8 : set search end ------------>>>");
    }

    public static void main(String[] args) {
        int loops = 0;
        int type = 0;
        int subtype = 0;
        try {
            type = Integer.parseInt(args[0]);
            subtype = Integer.parseInt(args[1]);
            loops = Integer.parseInt(args[2]);
        } catch(NumberFormatException nfe) {
            System.out.println("Need 3 argument, The arguments must be an integers.");
            System.exit(1);
        }

        switch(type){
            case 1:
                testRescursionSync(subtype, loops);
                break;
            case 2:
                testLoopSync(subtype, loops);
                break;
            case 3:
                testMultiThread(subtype, loops);
                break;
            case 4:
                testMultiObj(subtype, loops);
                break;
            case 5:
                testWaitSync(subtype, loops);
                break;
            case 6:
                testLockSync(subtype, loops);
                break;
            case 7:
                testSetSync(subtype, loops);
                break;
            case 8:
                testSetSearch(subtype, loops);
                break;
            default:
                testRescursionSync(subtype, loops);
        }
    }
}
