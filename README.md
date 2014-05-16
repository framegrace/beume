beume
=====

High performance, scalable and high available image resize service.

Motivation
----------
I've worked in a fair ammount of projects and at some point a service of this kind is needed. Looks so easy that usually is re-done everytime.
Another commont point in those projects is that resizing becomes a can of worms when in production.
Resizing is a kind of a costly operation and usually ends up in:
* batch processing images and serve them statically. This tends to cause integration problems and various logistic nuissances that causes rage on content managers and designers.
* Use a dynamic server to resize images on the fly. This involves generally a big service train of caches and services on more servers that would be desirable. That causes rage on sysadms and ops teams.
So my idea is just take the second point and make it more digerible by putting all in a single box.