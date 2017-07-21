#Indigo!

Indigo is a non-traditional game engine proof of concept.

Usually, game engines follow the Entity Component System (ECS) approach and this has yielded great results in all the most popular solutions.

There are a number of drawbacks (depending on your point of view) with the usual crop of engines that Indigo tries to address with varying levels of success - it's far from perfect.

Some of the issues are:
1. Older, error prone languages.
1. Difficulty in reasoning about the lifecycle of the game.
1. Compared to things like web development, relatively poor tooling experiences especially on non-windows platforms.

Too begin addressing these, Indigo uses:
1. Scala (which has it's own problems)
1. Functional programming and something inspired by the Functional Reactive Paradigm
1. Scala -> JS & WebGL as a compile target means cross platform dev tools are high quality. No special tools are needed but a few are supported.

##Functional Reactive Programming (FRP)
If you squint when you look at it: Indigo looks like an FRP engine and aims to behave like one. The hope is to move it further in that direction over time.

###What is FRP?
The most accessible description of FRP in frontend development (which games are), I think, is that it's a version of the MVC pattern that is **NOT** built using the Observer Pattern. The program flow is very simple:

1. The game is initialised with config and a model
1. On each frame:
    1. Game events (like the time) are passed to a model update function. The function takes the previous immutable version of the model and updates it creating a new, immutable instance.
    1. This model instance is then passed, read only, to a view function that knows how to render it.
    1. The view can generate events of it's own and these are passed to the event manager (controller?).
    1. All the while, the event manager is listening for both view events and external inputs and batches them up ready for the next frame.

####Win?
The beauty of this approach is it's simplicity. There is only one code path through the game / program. There are no event race conditions. There is no mutable state. The model can be processed lazily to aide calculations. Functions are all pure and easy to test. There is absolute separation of model, view, and controller concerns.

Sounds to good to be true right?

####...drawbacks of FRP
There are basically two issues with FRP.

The first is human: This isn't how we think about games. When you think "tank", years of conditioning will have you thinking about attaching a script / class to it to control it movements and receive events when it's hit by another tank's shells and so on. That's not how FRP works, that's the ECS pattern in action and to be fair, the ECS pattern is popular because it's how we tend to naturally (or by conditioning) think about the problem space.

The second is that the criticism generally levelled at FRP is about performance. FRP engines come in all shapes and sizes but all of them are considered / perceived to be slow. There are exceptions, Elm is one of the fastest web languages / frameworks around and it is FRP based (although they've stopped using the term FRP). FRP engines allocate / construct objects a lot and require a lot of associated object clean up. Mutation is always faster but harder to reason about.

Indigo still needs further performance tuning at the time of writing, but the reason it's been reference to here as being inspire by FRP is that the interface it's presents looks like an FRP interface, but the engine itself starts functional at a high level and becomes procedural down in it's guts so that it can work with WebGL. In between, there are a range of techniques used that FRP purists would be unhappy about but give us the acceptable performance levels we have at the moment.

The aim for indigo is to provide a lovely functional programming experience that is a delight to make games with and easy to reason about, while hiding all the nastiness out of sight.