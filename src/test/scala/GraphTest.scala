import net.whily.android.worldmetro.Graph
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import scala.collection.mutable

class Specs extends FunSpec with ShouldMatchers {
  describe("In class Graph") {
    it("find() should work for example in http://en.wikipedia.org/wiki/Dijkstra's_algorithm") {
      var map = new mutable.HashMap[(String, String), Int]()
      map += (("1", "2") -> 7)
      map += (("1", "3") -> 9)
      map += (("1", "6") -> 14)
      map += (("2", "3") -> 10)
      map += (("2", "4") -> 15)
      map += (("3", "4") -> 11)
      map += (("3", "6") -> 2)
      map += (("4", "5") -> 6)
      map += (("6", "5") -> 9)
      val graph = Graph.Graph(map)
      graph.find("1", List("5")) should be (List(List("1", "3", "6", "5")))
    }
  }
}
