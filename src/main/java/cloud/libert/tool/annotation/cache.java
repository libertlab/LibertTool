package cloud.libert.tool.annotation;

import cloud.libert.tool.cache.CacheType;

public @interface cache {
    CacheType type();

    /**
     * 一到多个(多个用;号隔开)key-value对，key和value是Entity实体的字段名称，每一对key-value可以是下面三种模式之一：
     * mId=$;    {mId}字段到实体对象本身的映射
     * mSyncId,mId,mCode=mValue;    在同一个Map中分别创建sy_{mSyncId},id_{mId},co_{mCode}到mValue字段的映射
     * mSyncId+mId=$;    {mSyncId}_{mId}作为组合key映射到实体对象
     */
    String rules();
}
