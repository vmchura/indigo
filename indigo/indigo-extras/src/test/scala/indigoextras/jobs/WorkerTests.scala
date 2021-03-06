package indigoextras.jobs

import indigo.shared.time.GameTime
import utest._
import indigoextras.jobs.SampleJobs.{CantHave, Fishing, WanderTo}

object WorkerTests extends TestSuite {

  val worker: Worker[SampleActor, SampleContext] = SampleActor.worker
  val actor: SampleActor                         = SampleActor(0, likesFishing = false)

  def tests: Tests =
    Tests {
      "You should be able to create a Worker instance" - {
        final case class TestActor()
        final case class TestContext()
        final case class TestJob() extends Job {
          val jobName: JobName = JobName("test job")
          val isLocal: Boolean = true
        }

        val isComplete: TestActor => Job => Boolean =
          _ => _ => true

        val onComplete: (TestActor, TestContext) => Job => JobComplete =
          (_, _) => _ => JobComplete.empty

        val doWork: (GameTime, TestActor, TestContext) => Job => (Job, TestActor) =
          (_, a, _) => j => (j, a)

        val jobGenerator: () => List[Job] =
          () => Nil

        val jobAcceptable: (TestActor, Job) => Boolean =
          (_, _) => false

        val worker = Worker.create[TestActor, TestContext](
          isComplete,
          onComplete,
          doWork,
          jobGenerator,
          jobAcceptable
        )

        val actor   = TestActor()
        val context = TestContext()
        val time    = GameTime.zero
        val job     = TestJob()

        worker.isJobComplete(actor)(job) ==> true
        worker.onJobComplete(actor, context)(job) ==> JobComplete.empty
        worker.workOnJob(time, actor, context)(job) ==> (job, actor)
        worker.generateJobs() ==> Nil
        worker.canTakeJob(actor)(job) ==> false
      }

      "A Worker instance" - {

        "should be able to check a job is complete" - {
          worker.isJobComplete(actor)(Fishing(Fishing.totalWorkUnits)) ==> true
        }

        "should be able to perform an action when a job completes" - {
          worker.onJobComplete(actor, SampleContext(false))(Fishing(Fishing.totalWorkUnits)).jobs.head ==> WanderTo(0)
        }

        "should be able to work on a job" - {
          val res = worker.workOnJob(GameTime.zero, actor, SampleContext(false))(Fishing(0))
          res ==> (Fishing(SampleActor.defaultFishingSpeed), actor)
        }

        "and working on a job can affect the actor" - {
          val res = worker.workOnJob(GameTime.zero, actor, SampleContext(true))(Fishing(0))
          res ==> (Fishing(SampleActor.defaultFishingSpeed), actor.copy(likesFishing = true))
        }

        "should be able to generate jobs" - {
          worker.generateJobs() ==> List(WanderTo(100))
        }

        "should be able to distinguish between jobs you can take and ones you can't" - {
          worker.canTakeJob(actor)(WanderTo(30)) ==> true
          worker.canTakeJob(actor)(CantHave()) ==> false
        }

      }
    }

}
