package com.github.reiseburo.rx.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

import java.io.IOException;

/**
 * PathChildren is an {@code Observable} which takes events from a {@code PathChildrenCache}
 * and emits them for subscription
 */
public class PathChildren {
    protected CuratorFramework curatorFramework;
    protected PathChildrenCache cache;

    protected PathChildren() {
    }

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


    public Observable<PathChildrenCacheEvent> watch(final String znodePath) {
        return Observable.create(new Observable.OnSubscribe<PathChildrenCacheEvent>() {
            @Override
            public void call(final Subscriber<? super PathChildrenCacheEvent> subscriber) {
                cache = new PathChildrenCache(curatorFramework, znodePath, true);
                try {
                    cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }

                cache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        subscriber.onNext(event);
                    }
                });
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                /* properly close out our cache object when our subscriber leaves */
                if (cache instanceof PathChildrenCache) {
                    try {
                        cache.close();
                    } catch (IOException exception) {
                        /* Swallow, we don't need this exception */
                    }
                }

            }
        });
    }
}
