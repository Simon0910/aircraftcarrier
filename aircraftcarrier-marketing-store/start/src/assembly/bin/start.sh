#!/bin/bash
#
# Usage: start.sh [debug]
#
[  -e `dirname $0`/env_app.sh ] && . `dirname $0`/env_app.sh
[  -e `dirname $0`/env_before.sh ] && . `dirname $0`/env_before.sh
[  -e `dirname $0`/env.sh ] && . `dirname $0`/env.sh
[  -e `dirname $0`/prepare.sh ] && . `dirname $0`/prepare.sh

if [ ! -d "$APP_LOG_HOME" ] ;then
    mkdir -p $APP_LOG_HOME
fi

if [ -n "$PID" ]; then
    echo "ERROR: The $APP_NAME already started!"
    echo "PID: $PID"
    exit 1
fi

echo "Starting the $APP_NAME ..."
echo "JAVA_HOME: $JAVA_HOME"
echo "APP_HOME: $APP_HOME"
echo "APP_LOG_HOME: $APP_LOG_HOME"
echo "STDOUT_FILE: $STDOUT_FILE"

#APP_STATIC_RESOURCE=$APP_HOME/static
#if [ -d "$APP_STATIC_RESOURCE" ] ;then
#    JAVA_OPTS="$JAVA_OPTS -Dspring.resources.static-locations=file://${APP_STATIC_RESOURCE}"
#    echo "Using APP_STATIC_RESOURCE:     $APP_STATIC_RESOURCE"
#fi

if [ -d "$APP_HOME" ]; then
    APP_LAUNCHER_JAR=`ls $APP_HOME | grep .jar`
    if [ -n "$APP_LAUNCHER_JAR" ]; then
        APP_LAUNCHER_JAR="$APP_HOME/$APP_LAUNCHER_JAR"
    fi
fi
echo "Using APP_LAUNCHER_JAR:     $APP_LAUNCHER_JAR"

#if [ -n "$ENCRYPTED_ENABLE" ]; then
#    #export LD_LIBRARY_PATH="$APP_HOME/library"
#    CLASSES_LIBRARY_OPTS="-agentpath:$APP_HOME/library/libClassesSecurity.so"
#    echo "Using CLASSES_LIBRARY_OPTS: $CLASSES_LIBRARY_OPTS"
#fi
echo "-----------"
echo "$JAVA_HOME/bin/java $CLASSES_LIBRARY_OPTS $SGM_OPTS $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS -cp $APP_CONF:$APP_LAUNCHER_JAR -jar $APP_LAUNCHER_JAR"
if [ -n "$FOREGROUND_MODE" ] ;then
    $JAVA_HOME/bin/java -XX:MetaspaceSize=256m -XX:+UseG1GC -XX:+PrintGCDetails $CLASSES_LIBRARY_OPTS $SGM_OPTS $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS -cp $APP_CONF:$APP_LAUNCHER_JAR -jar $APP_LAUNCHER_JAR
else
    nohup $JAVA_HOME/bin/java -XX:MetaspaceSize=256m -XX:+UseG1GC -XX:+PrintGCDetails $CLASSES_LIBRARY_OPTS $SGM_OPTS $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS -cp $APP_CONF:$APP_LAUNCHER_JAR -jar $APP_LAUNCHER_JAR > $STDOUT_FILE 2>&1 &
fi


COUNT=0
while [ $COUNT -lt 1 ]; do    
    sleep 1
    COUNT=`ps -ef | grep java | grep "$APP_HOME" | awk '{print $2}' | wc -l`
    echo "ps check count[$COUNT]"
    if [ $COUNT -gt 0 ]; then
        break
    fi
done

echo "OK!"
PID=`ps -ef | grep java | grep "$APP_HOME" | awk '{print $2}'`
#echo $PID > $PID_FILE
echo "PID: $PID"
sleep 1
#tail -200f $STDOUT_FILE