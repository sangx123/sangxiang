package com.sangxiang.app;

/**
 * Created by sangxiang on 13/4/17.
 */

public class MyGsonFormat {

    /**
     * qq : {"id":23,"version":0,"created":1491984442675,"updated":1491984442675,"platform":"qq","title":"测试2","content":"放假的时间发了卡数据"}
     * acquiesce : {"id":7,"version":0,"created":1491978382708,"updated":1491978496739,"platform":"acquiesce","title":"心开始","content":"心开始内容"}
     * weibo : {"id":17,"version":0,"created":1491979216049,"updated":1491983350694,"platform":"weibo","title":"测试1","content":"测试12323333"}
     * wechat : {"id":27,"version":0,"created":1491985569187,"updated":1491985569187,"platform":"wechat","title":"打法上","content":"放大书法示范"}
     */

    private QqBean qq;
    private AcquiesceBean acquiesce;
    private WeiboBean weibo;
    private WechatBean wechat;

    public QqBean getQq() {
        return qq;
    }

    public void setQq(QqBean qq) {
        this.qq = qq;
    }

    public AcquiesceBean getAcquiesce() {
        return acquiesce;
    }

    public void setAcquiesce(AcquiesceBean acquiesce) {
        this.acquiesce = acquiesce;
    }

    public WeiboBean getWeibo() {
        return weibo;
    }

    public void setWeibo(WeiboBean weibo) {
        this.weibo = weibo;
    }

    public WechatBean getWechat() {
        return wechat;
    }

    public void setWechat(WechatBean wechat) {
        this.wechat = wechat;
    }

    public static class QqBean {
        /**
         * id : 23
         * version : 0
         * created : 1491984442675
         * updated : 1491984442675
         * platform : qq
         * title : 测试2
         * content : 放假的时间发了卡数据
         */

        private int id;
        private int version;
        private long created;
        private long updated;
        private String platform;
        private String title;
        private String content;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public long getUpdated() {
            return updated;
        }

        public void setUpdated(long updated) {
            this.updated = updated;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class AcquiesceBean {
        /**
         * id : 7
         * version : 0
         * created : 1491978382708
         * updated : 1491978496739
         * platform : acquiesce
         * title : 心开始
         * content : 心开始内容
         */

        private int id;
        private int version;
        private long created;
        private long updated;
        private String platform;
        private String title;
        private String content;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public long getUpdated() {
            return updated;
        }

        public void setUpdated(long updated) {
            this.updated = updated;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class WeiboBean {
        /**
         * id : 17
         * version : 0
         * created : 1491979216049
         * updated : 1491983350694
         * platform : weibo
         * title : 测试1
         * content : 测试12323333
         */

        private int id;
        private int version;
        private long created;
        private long updated;
        private String platform;
        private String title;
        private String content;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public long getUpdated() {
            return updated;
        }

        public void setUpdated(long updated) {
            this.updated = updated;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class WechatBean {
        /**
         * id : 27
         * version : 0
         * created : 1491985569187
         * updated : 1491985569187
         * platform : wechat
         * title : 打法上
         * content : 放大书法示范
         */

        private int id;
        private int version;
        private long created;
        private long updated;
        private String platform;
        private String title;
        private String content;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public long getUpdated() {
            return updated;
        }

        public void setUpdated(long updated) {
            this.updated = updated;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
