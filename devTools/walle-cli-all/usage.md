Walle CLI 打渠道包工具命令行说明:

#获取当前apk中的渠道信息
java -jar walle-cli-all.jar show apk路径

#为签名的apk写入渠道信息（单个写入）
java -jar walle-cli-all.jar put -c 渠道信息 apk路径

#为签名的apk写入渠道信息(批量写入)
java -jar walle-cli-all.jar batch -f 渠道列表文件路径 apk路径

#指定输出文件，自定义名称。 不指定时默认与原apk包同目录。
java -jar walle-cli-all.jar put -c 渠道信息 原apk路径 目标生成路径

#命令行批量写入
java -jar walle-cli-all.jar batch -c 渠道信息1,渠道信息2,渠道信息3  apk路径

#指定渠道列表文件
java -jar walle-cli-all.jar batch -f 渠道列表文件路径  apk路径

配置文件示例：./channel（支持使用#号添加注释）


#https://github.com/Meituan-Dianping/walle/blob/master/walle-cli/README.md