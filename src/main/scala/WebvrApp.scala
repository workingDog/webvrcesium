import com.kodekutters._
import cesium._
import cesiumOptions._
import CesiumImplicits._

import org.scalajs.dom
import org.scalajs.dom.Navigator
import org.scalajs.dom.window

import scala.scalajs.js
import scala.scalajs.js.{Date, JSApp}
import scala.language.implicitConversions
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


/**
  * an example using WebVR Scala ("com.github.workingDog" %%% "webvrscala" % "0.1-SNAPSHOT") and
  * CesiumScala "com.github.workingDog" %%% "cesiumscala" % "1.4"
  *
  * ref: https://github.com/AnalyticalGraphicsInc/cesium/blob/master/Apps/Sandcastle/gallery/Cardboard.html
  *
  * To compile and generate this example, type "sbt fastOptJS".
  * This will generate "webvrtest-fastopt.js" in the "./target/scala-2.11" directory.
  * Put "webvrtest.html" and "webvrtest-fastopt.js" files in the "Cesium/Apps" directory and
  * launch Cesium (node server.js).
  *
  * Then point your browser to http://localhost:8080/Apps/webvrtest.html
  *
  */
object WebvrApp extends JSApp {

  import com.kodekutters.webvr._

  def main(): Unit = {

    Console.println("---> hello xx WebvrApp")

    // check we have a valid VR display
    window.navigator.getVRDisplays().toFuture onComplete {
      case Success(dispArr) =>
        if (dispArr.isEmpty)
          Console.println("display set is empty")
        else {
          for (display <- dispArr) Console.println("VRDisplay available: " + display.displayName)
          // start cesium with the first VR Display
          start(dispArr(0))
        }

      case Failure(f) => Console.println("fail to get any displays")
    }

  }

  def start(vrDisplay: VRDisplay): Unit = {
    // launch the Cesium viewer
    val viewer = new Viewer("cesiumContainer", ViewerOptions.vrButton(true))
    Console.println("isVREnabled " + viewer.vrButton.viewModel.isVREnabled)

    viewer.terrainProvider = new CesiumTerrainProvider(
      CesiumTerrainProviderOptions.
        url("//assets.agi.com/stk-terrain/world").
        requestVertexNormals(true))

    viewer.scene.globe.enableLighting = true
    viewer.scene.globe.depthTestAgainstTerrain = true

    // Follow the path of a plane. See the interpolation Sandcastle example.
    val start = JulianDate.fromDate(new Date(2015, 2, 25, 16))
    val stop = JulianDate.addSeconds(start, 360, new JulianDate())

    viewer.clock.startTime = start.clone()
    viewer.clock.stopTime = stop.clone()
    viewer.clock.currentTime = start.clone()
    viewer.clock.clockRange = ClockRange.LOOP_STOP
    viewer.clock.multiplier = 1.0

    // returns a SampledPositionProperty
    def computeCirclularFlight(lon: Double, lat: Double, radius: Double) = {
      val property = new SampledPositionProperty()
      val startAngle = Math.random() * 360.0
      val endAngle = startAngle + 360.0
      var increment = (Math.random() * 2.0 - 1.0) * 10.0 + 45.0
      for (i <- startAngle until endAngle by increment) {
        val radians = Math.toRadians(i)
        val timeIncrement = i - startAngle
        val time = JulianDate.addSeconds(start, timeIncrement, new JulianDate())
        val position = Cartesian3.fromDegrees(lon + (radius * 1.5 * Math.cos(radians)), lat + (radius * Math.sin(radians)), Math.random() * 500 + 1750)
        property.addSample(time, position)
      }
      property
    }

    // initial location
    val longitude = -112.110693
    val latitude = 36.0994841
    val radius = 0.03
    val radians = 0.03

    val modelURI = "SampleData/models/CesiumBalloon/CesiumBalloon.glb"

    // the ballon we (the camera) are in
    var entity = viewer.entities.add(new Entity(EntityOptions.
      position(Cartesian3.fromDegrees(longitude, latitude, radius * 500 + 1750)).
      model(new ModelGraphics(ModelGraphicsOptions.
        uri(modelURI).
        minimumPixelSize(64)))
    ))

    // set initial camera position and orientation to be in the model's reference frame.
    var camera = viewer.camera
    camera.position = new Cartesian3(0.25, 0.0, 0.0)
    camera.direction = new Cartesian3(1.0, 0.0, 0.0)
    camera.up = new Cartesian3(0.0, 0.0, 1.0)
    camera.right = new Cartesian3(0.0, -1.0, 0.0)

    // callback by the rendering
    def callback = (scene: Scene, time: JulianDate) => {
      val position = entity.position.getValue(time)
      val pose = vrDisplay.getPose()
      val ori = pose.orientation

      var transform = Matrix4.IDENTITY
      if (Cesium.defined(ori)) {
        val q = if (ori(0) == 0 && ori(1) == 0 && ori(2) == 0 && ori(3) == 0) Quaternion.IDENTITY else new Quaternion(ori(0), ori(1), ori(2), ori(3))
        transform = Matrix4.fromRotationTranslation(Matrix3.fromQuaternion(q), position)
      } else {
        transform = Transforms.eastNorthUpToFixedFrame(position)
      }

      // save camera state
      val offset = Cartesian3.clone(camera.position)
      val direction = Cartesian3.clone(camera.direction)
      val up = Cartesian3.clone(camera.up)

      // set camera to be in model's reference frame.
      camera.lookAtTransform(transform)

      // Reset the camera state to the saved state so it appears fixed in the model's frame.
      Cartesian3.clone(offset, camera.position)
      Cartesian3.clone(direction, camera.direction)
      Cartesian3.clone(up, camera.up)
      Cartesian3.cross(direction, up, camera.right)
    }

    viewer.scene.preRender.addEventListener(callback)

    // add a few more balloons flying around
    for (i <- 0 until 12 by 1) {
      var balloonRadius = (Math.random() * 2.0 - 1.0) * 0.01 + radius

      var balloon = viewer.entities.add(new Entity(EntityOptions.
        availability(new TimeIntervalCollection(js.Array(new TimeInterval(TimeIntervalOptions.
          start(start).
          stop(stop))))).
        position(computeCirclularFlight(longitude, latitude, balloonRadius)).
        model(new ModelGraphics(ModelGraphicsOptions.
          uri(modelURI).
          minimumPixelSize(64)))
      ))

      balloon.position.asInstanceOf[SampledPositionProperty].setInterpolationOptions(
        InterpolationOptions.
          interpolationDegree(2).
          interpolationAlgorithm(HermitePolynomialApproximation))
    }

  }

}

