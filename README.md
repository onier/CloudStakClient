# CloudStakClient
java CloudStakClient
此代码只演示了一个最简单的java调用cloudstack api的功能， 
通过session方式连接到cloudstack,并且获取key，然后使用key方式调用api。

获取key可能会导致webui以登陆用户退出，并且拿到的key可能与webui显示的key不一致，但是不影响使用。

public static KeyStore getCloudStackApiKey(String url, String name, String password)
次方法为session登陆获取key，每次一次调用都会产生新的key，原有的key失效。

秉着最简化的代码，剥离了httpclient，直接用拼凑请求字符串完成。

仅供参考使用。
