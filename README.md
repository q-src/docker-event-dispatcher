# docker-event-dispatcher

[![Docker Build Status](https://img.shields.io/docker/build/qsrc/event-dispatcher.svg?style=for-the-badge)](https://hub.docker.com/r/qsrc/event-dispatcher/)
[![Docker Stars](https://img.shields.io/docker/stars/qsrc/event-dispatcher.svg?style=for-the-badge)](https://hub.docker.com/r/qsrc/event-dispatcher/)
[![Docker Pulls](https://img.shields.io/docker/pulls/qsrc/event-dispatcher.svg?style=for-the-badge)](https://hub.docker.com/r/qsrc/event-dispatcher/)


This is an event dispatcher for file based docker inter-container communication. It enables docker containers to trigger 
actions in other containers by simply creating files. 


## Concept

The event emitting container ("emitter") and the event dispatcher container ("event-dispatcher") need to share a volume.
When the emitter creates a file inside this volume, the dispatcher gets triggered. It creates an event with an id 
corresponding to the path of the created file and notifies all subscribed containers ("event handlers"). 
For example, if a file `$event_volume/some-dir/some-sub-dir/the-event-file` is created, all event handlers subscribed to
 event `some-dir.some-sub-dir.the-event-file` get notified.

The event-dispatcher removes every event file after successful dispatching.

Subscribing a container to an event is easy and can be done using container labels. A container is subscribed on 
events with id `${eventId}` when it has a `subscription.${eventId}` label. 
The subscription usually defines a command, which will be executed inside the container using `docker exec`. For further
details have a look at the [Event Handler Configuration](#Event-Handler-Configuration) section.


## Usage

A typical event dispatcher can be started as follows:

```bash
docker run \
  -v $PWD/events:/app/events \
  -v /var/run/docker.sock:/run/docker.sock \
  qsrc/event-dispatcher
```

### Volumes
* `/app/events`: This volume is watched for events (files). Every event emitting container needs to write into this 
volume for emitting an event.
* `/run/docker.sock`: The event-dispatcher expects the host's docker socket being mounted on this volume. Otherwise,
event handling containers can not be notified.

### Event Handler Configuration
Event handling containers can be subscribed to events by specifying `subscription.*` labels (the namespace can be 
[changed](#DISPATCHER_LABEL_NAMESPACE)).

In the following example, `docker exec event-handler touch handled` is called when the event `demo.some-event` occurs:
```bash
docker run -ti --name  event-handler -l subscription.demo.some-event.container.command=touch\ handled alpine sh

```

The following section describes all supported labels for a more specific configuration (each label must be prefixed with
the corresponding label namespace `${namespace}`, see also 
[DISPATCHER_LABEL_NAMESPACE](#DISPATCHER_LABEL_NAMESPACE)).

#### `${namesapce}.${eventId}.container.command` | Default: ` `

Specifies the command which shall be executed inside the event handler container when the event with id `${eventId}`
occurs. 

#### `${namesapce}.${eventId}.container.start` | Default: `false`
If set to `true`, the event handler container gets started if it is not running and an event with id `${eventId}`
occurs. 

#### `${namesapce}.${eventId}.dispatching.debounce` | Default: `0` (in milliseconds)
If an event occurs very often, event handlers can instruct the dispatcher to debounce their notifications.

By setting this label to `${value}`, the event handler gets only notified, if the last event with id `${eventId}` 
occurred `${value}` milliseconds ago.

For example if you specify `${namesapce}.${eventId}.dispatching.debounce=1000`, the container will receive notifications
with a maximum delay of 1000ms. If an event with id `${eventId}` occurs 800ms after the last notification, it will be 
dispatched to the container 200ms later if no other event with this id occurs. All occurrences in-between are ignored. 
So only the last event during 1000ms since the last notification is dispatched. 

The first event with id `${eventId}` since the dispatcher was started is always dispatched immediately.


### Event Dispatcher Configuration
The event-dispatcher can be configured using the following environment variables:

#### <a name="DISPATCHER_LABEL_NAMESPACE"></a> `DISPATCHER_LABEL_NAMESPACE` | Default: `subscription`
Specifies the label namespace. Every container label set on event handling containers needs to be prefixed with the 
value of this setting. See also [Event Handler Configuration](#Event-Handler-Configuration).

#### `DISPATCHER_FORWARD_DESTINATION` | Default: `direct:null` (no forwarding)
Every event detected by the event-dispatcher can be forwarded to any endpoint supported by Apache Camel.
Multiple destinations are supported and must be separated by the delimiter character (see `DISPATCHER_FORWARD_DELIMITER`).

#### `DISPATCHER_FORWARD_DELIMITER` | Default: `,`
Specifies the delimiter character which separates multiple forwarding destinations specified in `DISPATCHER_FORWARD_DESTINATION`.

## Contribute
In order to contribute a patch, the workflow is as follows:

1. Fork the repository
2. Create a feature branch
3. Commit patches
4. Submit pull request

Please add unit tests for every feature added. In case of bugs, please add a unit test proving that the bug existed and
is fixed.

In general commits should be atomic and diffs should be easy to read. For this reason do not mix any formatting fixes or
code moves with actual code changes.

Commit messages should be verbose by default. TheyC should be helpful to people reading your code in the future, so
explain the reasoning for your decisions. Further explanation [here](http://chris.beams.io/posts/git-commit/).

## License
This software is released under the MIT license.

## Donations
Do you like this project and want to say thanks? Your donation is always welcome:

Bitcoin: `1LHq6uYsqMX23tx2hDripE4zktX6yxfrUF`
