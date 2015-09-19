package com.github.reiseburo.rx.curator

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent
import org.apache.curator.retry.RetryOneTime
import org.apache.curator.test.TestingServer
import rx.Observable
import rx.Subscription
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import spock.lang.*
import spock.util.concurrent.PollingConditions

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

/**
 * This test will spin up an in-process Zookeeper server for testing events that
 * get fired off through the PathChildren observer
 */
class PathChildrenIntegrationSpec extends Specification {
    CuratorFramework curator
    TestingServer server
    TestSubscriber subscriber
    PollingConditions pollingConditions
    boolean eventReceived = false

    def setup() {
        server = new TestingServer(true)
        curator = CuratorFrameworkFactory.newClient(server.connectString, new RetryOneTime(1000))
        curator.start()
        subscriber = new TestSubscriber()
        pollingConditions = new PollingConditions(timeout: 10)
    }

    def cleanup() {
        eventReceived = false
        curator?.close()
        server?.close()
    }

    Observable<PathChildrenCacheEvent> createObservableFor(CuratorFramework curator, String path) {
        return PathChildren.with(curator).watch(path)
    }

    def "an initialization event should be received"() {
        when:
        createObservableFor(curator, '/').subscribe { PathChildrenCacheEvent ev ->
            if (ev.type == PathChildrenCacheEvent.Type.INITIALIZED) {
                eventReceived = true
            }
        }

        then:
        pollingConditions.eventually { assert eventReceived }
    }

    def "receive normal add events"() {
        given:
        createObservableFor(curator, '/').subscribe { PathChildrenCacheEvent ev ->
            if (ev.type == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                eventReceived = true
            }
        }

        when:
        curator.create().forPath('/spock')

        then:
        pollingConditions.eventually { assert eventReceived }
    }

    def "receive delete events"() {
        given:
        final String znode = '/spock'
        curator.create().forPath(znode)

        createObservableFor(curator, '/').subscribe { PathChildrenCacheEvent ev ->
            if (ev.type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                eventReceived = true
            }
        }

        when:
        curator.delete().forPath(znode)

        then:
        pollingConditions.eventually { assert eventReceived }
    }
}
