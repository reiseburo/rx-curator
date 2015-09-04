package com.github.reiseburo.rx.curator

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent
import rx.Observable
import spock.lang.*

/**
 */
class PathChildrenSpec extends Specification {
    CuratorFramework curatorFramework

    def setup() {
        curatorFramework = Mock(CuratorFramework)
    }

    def "PathChildren.with(curator) returns an instance"() {
        expect:
        PathChildren.with(curatorFramework) instanceof PathChildren
    }

    def ".watch(String) should return an Observable<T>"() {
        expect:
        PathChildren.with(curatorFramework).watch('/brokers') instanceof Observable<PathChildrenCacheEvent>
    }
}
