package com.github.reiseburo.rx.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import rx.Observable;

/**
 * PathChildren is an {@code Observable} which takes events from a {@code PathChildrenCache}
 * and emits them for subscription
 */
public class PathChildren {
    protected CuratorFramework curatorFramework;

    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    public void setCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }


    /**
     * Create an instance of PathChildren configured with the provided
     * {@code CuratorFramework} instance. This assumes that {@code curatorFramework}
     * has already been started
     *
     * @param curatorFramework
     * @return
     */
    public static PathChildren with(CuratorFramework curatorFramework) {
        PathChildren instance = new PathChildren();
        instance.setCuratorFramework(curatorFramework);
        return instance;
    }


    public Observable<PathChildrenCacheEvent> watch(String znodePath) {
        return Observable.never();
    }
}
