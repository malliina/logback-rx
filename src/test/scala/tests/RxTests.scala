package tests

import com.malliina.rx.BoundedReplaySubject
import org.scalatest.FunSuite
import rx.lang.scala.Observable
import rx.lang.scala.subjects.{AsyncSubject, BehaviorSubject, ReplaySubject}

import scala.concurrent.duration.DurationInt

class RxTests extends FunSuite {
  test("can run test") {

  }

  test("can do rx") {
    val intObs = Observable.interval(100.milliseconds)
    val firstFive = intObs.take(5).toBlocking.toList
    assert(firstFive === List(0, 1, 2, 3, 4))
  }

  test("ReplaySubject caches all values") {
    val subject = ReplaySubject[Int]()
    subject.onNext(5)
    subject.onNext(6)
    subject.onCompleted()
    assert(subject.toBlocking.toList === List(5, 6))
  }

  test("BehaviorSubject caches the latest event: a value, error or completed") {
    val obs = BehaviorSubject[Int](0)
    obs.onNext(1)
    assert(obs.first.toBlocking.toList === List(1))
    val sub1 = obs.subscribe(i => assert(i === 1))
    sub1.unsubscribe()
    obs.onNext(2)
    val sub2 = obs.subscribe(i => assert(i === 2))
    sub2.unsubscribe()
    obs.onCompleted()
    // onCompleted is the latest event, toList reduces it to nothing
    assert(obs.toBlocking.toList === Nil)
  }

  test("AsyncSubject emits the final value") {
    val obs = AsyncSubject[Int]()
    val sub1 = obs.subscribe(i => throw new Exception) //assert(i === 1))
    sub1.unsubscribe()
    obs.onNext(2)
    val sub2 = obs.subscribe(i => assert(i === 2))
    sub2.unsubscribe()
    obs.onCompleted()
    assert(obs.toBlocking.toList === List(2))
  }

  test("BoundedReplaySubject") {
    val bounded = BoundedReplaySubject[Int](2)
    bounded.onNext(1)
    bounded.onNext(2)
    bounded.onNext(3)
    //    bounded.subscribe(v => println(v))
    bounded.onNext(4)
    bounded.onCompleted()
    assert(bounded.toBlocking.toList === List(3, 4))
  }

  test("three first of obs1, followed by obs2") {
    val obs1 = Observable.just(1, 2, 3, 55, 66)
    val obs2 = Observable.just(4, 5, 6)
    val o = obs1.take(3) ++ obs2
    assert(o.toBlocking.toList === List(1, 2, 3, 4, 5, 6))
  }

  //  test("latest, with subject") {
  //    val obs = BehaviorSubject(0)
  //    obs.subscribe(v => println(v))
  //    obs.onNext(1)
  //    assert(obs.first.toBlockingObservable.toList === Seq(1))
  //    obs.onNext(2)
  //    obs.onCompleted()
  //  }

  test("Observable.apply") {
    val o: Observable[Int] = Observable { observer =>
      observer.onNext(1)
      observer.onCompleted()
    }
    assert(o.toBlocking.toList === Seq(1))
  }

  test("Observable.takeLast") {
    val r = ReplaySubject[Int]()
    r.onNext(1)
    r.onNext(2)
    r.onNext(3)
    r.onCompleted()
    assert(r.takeRight(2).toBlocking.toList === List(2, 3))
  }

  test("Observable.from") {
    val obs = Observable.from(List(1, 2, 3))
    assert(obs.toBlocking.toList === List(1, 2, 3))
  }

  test("Delist") {
    val o1 = Observable.just(List(1, 2, 3))
    val o2 = o1.first.flatMap(list => Observable.from(list))
    assert(o2.toBlocking.toList === List(1, 2, 3))
  }

  test("Delist a BehaviorSubject") {
    val historySubject = BehaviorSubject[List[Int]](Nil)
    val o2 = historySubject.first.flatMap(seq => Observable.from(seq))
    o2.subscribe((e: Int) => println(e))
  }
}
