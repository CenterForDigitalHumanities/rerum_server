# rerum_server
Java web service for a RERUM compliant digital object repository.
Visit [rerum.io](http://rerum.io) for more general information.
Want to use the API?  Learn how at the [API page](https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md).

Stores important bits of knowledge in structured JSON-LD objects:

* Web Annotation / Open Annotation objects
* SharedCanvas / International Image Interoperability Framework objects
* FOAF Agents
* _any_ valid JSON object, even if there is no type specified!

## Basic Principles

1. **As RESTful as is reasonable**—accept and respond to a broad range of requests without losing the map;
1. **As compliant as is practical**—take advantage of standards and harmonize conflicts;
1. **Save an object, retrieve an object**—store metadata in private (`__rerum`) property, rather than wrap all data transactions;
1. **Trust the application, not the user**—avoid multiple login and authentication requirements and honor open data attributions;
1. **Open and Free**—expose all contributions immediately without charge to write or read;
1. **Attributed and Versioned**—always include asserted ownership and transaction metadata so consumers can evaluate trustworthiness and relevance.

## What we add

You will find a `__rerum` property on anything you read from this repository. This is written onto
all objects by the server and is not editable by the client applications. While applications may assert
_anything_ within their objects, this property will always tell the Truth. The details are in the
documentation, but broadly, you will find:

* `created`  specific creation date for this \[version of this] object
* `isOverwritten`  specific date (if any) this version was updated without versioning
* `generatedBy`  the agent for the application that authenticated to create this object
* `isReleased`  a special flag for RERUM, indicating this version is intentionally public and immutable
* `releases`  an object containing the most recent anscestor and descendant releases
* `history`  an object containing the first, previous, and immediate derivative versions of this object

## Who is to blame?

The developers at the Walter J. Ong, <sub><sup>S.J.</sup></sub> Center for Digital Humanities authored and maintain this service.
Neither specific warranty or rights are associated with RERUM; registering and contributing implies only those rights 
each object asserts about itself. We welcome sister instances of RERUM, ports to other languages, package managers, builds, etc.
Contributions to this repository will be accepted as pull requests.
