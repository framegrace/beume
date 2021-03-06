beume
=====

High performance, scalable and high available java based image resize service.

VERY EARLY STAGE 
----------------

Please test it. I will be very happy if you tell me the results. Will do my best to invite you to a Beer if you ever go to Barcelona, and we'll chat about it or about anything.


Motivation
----------
Beume takes a source image, resizes it on the fly, and serves it directly to the client machine caching everything in the process. A pretty simple task.

I've worked in a fair amount of projects and a service of this kind is always needed at some point. Looks so easy that usually is re-done everytime.

Another comon point in those projects is that resizing becomes a can of worms when in production.

Resizing is a kind of a costly operation and usually ends up in:

* batch processing images and serve them statically. This tends to cause integration problems and various logistic nuissances for content managers and designers.
* Use a dynamic server to resize images on the fly. This involves generally a big service train of caches and services on more servers that would be desirable. That causes rage on sysadmin and ops teams.

I've been looking for a solution to this problem on all the internets and found a little or nothing. So I've created one.

I've tried to solve all the problems of the task in the simpler and more efficient way possible.

Beume is a front-end cache, a dynamic resizer and a back-end cache on a single service. It can scale horizontally and vertically with full high availability.

Features
--------

* Have very little code of my own. The service is just a small binding code between grizzly, ehcache and some resizing libraries. Reinvented the less wheels as possible.
* Fully Multi-Threaded NIO server. From end to end. Some resize libraries are even capable of parallell resizing. It will burn all your CPUS.
* Capable of all the things ehcache is capable. That includes features very suited for this resizing task: No Cache Stampede, Lateral caching for sibling server update, disk backing store, auto discovery.... 
* Simple. One jar and a couple of config files. (Well, ehcache one is not that simple....)
* A (minimal right now) admin page.

A bad drawn diagram of Beume architecture, showing also the fanciness of lateral cache:

![Beume diagram](imgs/Beume diagram.jpg)

A miss of any image, will be populated to all the servers of the cluster. So in case of a failure of one of the servers, all the rest have all the content. It also makes sharding unnecessary performance-wise (All images are calculated only once). You can still use sharding for memory constrains (If you want. allthough this have to be configured outside, on the load balancer)

To configure lateral cache, please read the ehcache documentation. It's all on its roof, beume has no word on it (Ah! the joys of minimalistic programming :) )
To-Do
-----
* Able to prune and pin entries.
* Better admin page (With pruning and pining and cached image inspection)
* Modularize resizers. Some sort of plugin mechanism.
* Simplify ehcache configuration.
* Use a whitelist for source urls. (Now it's an open resizer, which is no good)
* Unit testing. (This has been a quick hack that scaled quickly, so no time to explain...)

Install
-------
* Download the latest release (release dir). It includes all dependencies (ehcache,resize libs,grizzly,slf4j and some Apache commons and httpclient. See the dependecies list below)
* Alternatively, you can clone the project and open it in Netbeans (Is a Netbeans project) or use maven to rebuild it.
* Create a directory called beume on your home dir and put a beume.properties and an ehcache.xml. This is the default beume.properties, and an ehcache sample config file to test. (You can place the files elsewhere, just specify the propertires file on startup and change the EHCACHE_CONF_FILE option inside)

### ~/beume/beume.properties

****

    # EHCACHE_CONF_FILE=/someuser/beume/ehcache.xml # Defaults to <Homedir>/beume
    BACK_ERROR_RETAIN_SECS=30
    HOST=localhost
    BASE_URL=/
    PORT=8080
    ADM_BASE_URL=/
    ADM_PORT=8085
    BACK_REQ_MAX_LENGTH=4194304
    ADM_HOST=localhost
    DEFAULT_FILTER=lanczos
    FRONT_ERROR_RETAIN_SECS=10    

****

Almost all parameters are pretty obvious. Here are the maybe most confusing ones:

* BACK/FRONT_ERROR_RETAIN_SECS: Any error retrieving the source image will stay on the caches for that amount of time. After that, Beume will retry the retrieval. This is to avoid hammering the server with failed image retrievals. During this time, Beume will return the cached error on any query.
* BACK_REQ_MAX_LENGHT: Maximum source image size. Security feature to avoid caching an Ubuntu ISO or a DVD rip by accident. This will sure destroy the server. ( Baume needs to buffer all the source image at once, in memory)

### ~/beume/ehcache.xml (Basic config)

    <ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../../main/config/ehcache.xsd" name="cm2">
    <diskStore path="java.io.tmpdir/one"/>
    
    <defaultCache maxElementsInMemory="1000" eternal="false" timeToIdleSeconds="100" timeToLiveSeconds="100" >
    </defaultCache>
    
    <cache name="frontCache" maxElementsInMemory="100" eternal="false" timeToIdleSeconds="100" timeToLiveSeconds="100" memoryStoreEvictionPolicy="LRU">
    </cache>
    <cache name="backCache" maxElementsInMemory="500" eternal="false" timeToIdleSeconds="300" timeToLiveSeconds="600" memoryStoreEvictionPolicy="LRU">
    </cache>
    </ehcache>

****

See ehcache documentation for more complex configurations. The only constraint is that the caches named "frontCache" and "backCache" must be defined.

* Run the jar 

    java -jar beume-1.0.jar 

* Or alternatively, for a config file on a different place:

    java -jar beume-1.0.jar -- /etc/beume/beume.properties


And that's it, point the browser to <host>:8080 and put an URL like this one:

    http://<host>:8080/cidi.jpg?source=<Source url>&resize=<WidthxHeight>&filter=<Lanczos|imgscalr|thumbnailer>&q=<jpeg quality 1-100>

Admin/Stats page is at:

    http://<host>:8085/stats

Filters:
* lanczos: Uses the algorithm from this guy https://code.google.com/p/java-image-scaling/. Is faster than imgscalr (although, very little) and with the best quality (For me) of the three. Uses a multithreaded Lanczos3 algorithm. I presume the multithreading is the responsible of the small speed increase, so in load this may be negligible. It does not maintain image proportions, will do exactly wat you say, scale to WxH.
* imgscalr: Pretty solid scaling algorithm from here: http://www.thebuzzmedia.com/software/imgscalr-java-image-scaling-library/. Is almost as fast as the above with my (harcoded) settings. A little too sharp results for me. Maybe need more tweaking of the options. Always maintains image proportions. Will scale to fit the box you define in WxH.
* thumbnailer (or whatever not lanczos or imgscalr) : Uses Thumbnailator (https://code.google.com/p/thumbnailator/), it doesn't explain the algorithm used. Is the fastest, but the results are not as good as the previous ones for me. (Again, this is a matter of personal tast). It does not maintain image proportions.

Dependencies used
-----------------

All those are available thru default maven repository.

      junit 3.8.1
      thumbnailator 0.4.6
      ehcache 2.7.4
      java-image-scaling 0.8.5
      commons-httpclient 3.1
      slf4j-jdk14 1.7.5
      slf4j-simple 1.7.0
      jersey-grizzly2 1.8
      asm 3.1
      jersey-bundle 1.8
      imgscalr-lib 4.2

Final note
-----------

I've still not fully tested the code in full production loads and with all the full fledged ehcache tweakery for HA and scaling. Just basic tests. Reviewers are welcomed

Hope is usefull for you!!

As usual, use at your own risk. I make no guarantees nor take any responsibility of any downtime caused.
