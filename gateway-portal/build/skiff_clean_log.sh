#!/usr/bin/env bash
# Author: zhaowenyu@corp.netease.com
# CreateDate: 2019/10/15
# CurrentVersion: v1.0
# Descripte:
# 环境变量要求：
# 1. 以 SKIFF_LOGFILE 开头，例如 SKIFF_LOGFILE_0/SKIFF_LOGFILE_1 或 SKIFF_LOGFILE_CATALINA
# 2. 以竖线|分隔变量值
# 3. 第二个值目前支持保留数量num、和保留天数day
# 4. 第三个值就是具体num或day
# 环境变量EG：SKIFF_LOGFILE1="/ssddisk1/vinny/qingzhou-dockerfile/cleanlog/a.log|num|3"
logfile_list=$(cat $CATALINA_HOME/bin/envStorage|grep -E "^SKIFF_LOGFILE"|awk -F'=' '{print $NF}' )

# 遍历所有已SKIFF_LOGFILE*开头的env环境变量
for i in $logfile_list
do
    log_file=$(echo $i | awk -F'|' '{print $1}')
    clean_type=$(echo $i | awk -F'|' '{print $2}')
    type_value=$(echo $i | awk -F'|' '{print $3}')

    # 如果变量内容为空，则直接忽略
    if [ -z "$log_file" -o -z "$clean_type" -o -z "$type_value" ];then
        echo "SKIFF_LOGFILE config invalid: $i"
        continue
    fi

    # 如果对文件处理方式是保留一定的数量num时
    if [ "$clean_type" = "num" ];then
        num_add_1=$(( ${type_value} + 1 ))
        for k in $(ls ${log_file} |sort -r|tail -n +${num_add_1} )
        do
            # 如果文件被占用，则跳过
            ([[ $(which fuser) && ! -z $(fuser ${k}) ]]) &&  continue
            echo $k
            rm -f $k
        done

    # 如果对文件处理方式是保留一定的天数day时
    elif [ "$clean_type" = "day" ];then
        # now_time 当前系统Unix时间戳
        # save_time 是保留几天的文件的时间点
        now_time=$(date +%s)
        save_time=$(($now_time - $type_value * 24 * 3600))

        for j in $(ls ${log_file})
        do
            # 文件内容最后修改的Unix时间戳
            log_modify_time=$(stat -c "%Y" $j)

            if [ $log_modify_time -lt $save_time ];then
                # 如果文件被占用，则跳过
                ([[ $(which fuser) && ! -z $(fuser ${j}) ]]) &&  continue

                echo $j
                rm -f $j
            fi
        done
    fi
done
