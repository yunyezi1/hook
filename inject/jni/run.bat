adb push ..\libs\armeabi\libTest.so  /mnt/sdcard/
adb push ..\libs\armeabi\libso.so  /mnt/sdcard/
adb push ..\libs\armeabi\inject /mnt/sdcard/
# adb shell su chmod 777 /mnt/sdcard/inject
# adb shell su chmod 777 /mnt/sdcard/libso.so
# adb shell su chmod 777 /mnt/sdcard/libTest.so
# adb shell su -c /mnt/sdcard/inject
pause