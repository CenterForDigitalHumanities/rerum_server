# API (0.9)

- [API](#api)
    - [GET](#get)
        - [GET by ID](#single-object-by-id)
        - [GET by custom query](#get-by-custom-query)
    - [POST](#post)
        - [Create](#create)
        - [Batch Create](#batch-create)
    - [PUT](#put)
        - [Update](#update)
        - [Batch Update](#batch-update)
    - [PATCH](#patch)
        - [Update](#patch-update)
        - [Set/Unset](#patch-set)
    - [DELETE](#delete)
        - [Delete](#delete)
    - [Smart objects](#smart-objects)
    - [__rerum](#__rerum)
    - [REST](#rest)
    - [IIIF](#iiif)
    - [Web Annotation](#web-annotatio)
    - [Error Responses](#rerum-responses)

All the following interactions will take place between
the server running RERUM and the application server. If
you prefer to use the public RERUM server (which I hope
you do), the base URL is `http://rerum.io/rerumserver`. 

## GET

### Single object by id

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/id/@id` | `empty` | 200: JSON \[obj]
| | `[{JSON}]` | 404: "Does not exist in RERUM"

- **`@id`**—the @id of the object in RERUM.
- Call over HTTP can be made through GET request to their
unique URL Ex. http://rerum.io/rerumserver/id/aee33434bbc333444ff

### History tree since ID
As objects in RERUM are altered, their previous state is saved through
a history tree.  You can ask for all child versions of a given object.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/since/_id` | `empty` | 200: `[{JSON}]`

- **`@id`**—the id of the object in RERUM.
- Call over HTTP can be made through GET request to their
unique URL Ex. http://rerum.io/rerumserver/since/aee33434bbc333444ff

### History tree before ID
As objects in RERUM are altered, their previous state is saved through
a history tree.  You can ask for all parent versions of a given object.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/history/_id` | `empty` | 200: `[{JSON}]`

- **`_id`**—the id of the object in RERUM.
- Call over HTTP can be made through GET request to their
unique URL Ex. http://rerum.io/rerumserver/history/aee33434bbc333444ff

#### By custom query

The bulk of any application's interactions with RERUM will be
in the queries. This simple format will be made more complex
in the future, but should serve the basic needs as it is.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/getByProperties.action` | `{JSON}` | 200: JSON [obj]
| | `[{JSON}]` | 404: "No records exist in RERUM"

All responses are in a JSON Array, even if only a single
record is returned. Submissions must be queries in the form of JSON. 
RERUM will test for property matches, so `{ "@type" : "sc:Canvas", "label" : "page 46" }` will match

~~~ (json)
{
  "@id": "https://rerum.io/rerumserver/id/ae33ffee5656789",
  "otherContent": [],
  "label": "page 46",
  "width": 730,
  "images": [],
  "height": 1000,
  "@type": "sc:Canvas"
}
~~~

## POST

### Create

Add a completely new object to RERUM and receive the location
in response.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/create.action` | `{JSON}` | 201: `header.Location` "Created @ `[@id]` `[{JSON}]`

Accepts only single JSON objects for RERUM storage. Mints a
new URI and returns the object's location as a header. If the
object already contains an `@id` that matches an object in RERUM,
the API will direct the user to use [update](#update) instead.

- **`[{JSON}]`**—Containing various bits of information about the create.  The object looks like
~~~ (json)
{
  "code" : 201,
  "@id" : "http://rerum.io/rerumserver/id/5a5f6f06e4b02339378b8976",
  "iiif_validation" : {
    "warnings" : ["Array of warnings from IIIF validator"],
    "error" : "Error for why this object failed validation",
    "okay" : 1 // 0 or 1 as to whether or not it passed IIIF validation
  }
}

~~~

### Batch Create

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/batchCreate.action` | `[{JSON}]` | 200: "`[@id]`" 

The array of JSON objects passed in will be created in the
order submitted and the response will have the URI of the new
resource or an error message in the body as an array in the
same order.

When errors are encountered, the batch process
will attempt to continue for all submitted items.


## PUT

### Update

Replace an existing record through reference to its internal
RERUM id.  This will have the effect of set and unset actions.  
New keys will be created and keys notpresent in the request will be dropped.  
When an object is updated, the `@id` will be changed, as the previous
version will maintain its place in the history of that object.
 __rerum, @id and ObjectID updates are ignored.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/put_update.action` | `{JSON}` | 200: `header.Location` New state `[{JSON}]`

- **`[{JSON}]`**—Containing various bits of information about the PUT update.  The object looks like
~~~ (json)
{
  "code" : 200,
  "original_object_id" : "http://devstore.rerum.io/rerumserver/id/5a57a30fe4b09163a80a0a67",
  "new_obj_state" : {
    @id: newID
    .
    .
    .
  },
  "iiif_validation" : {
    "warnings" : [ ],
    "error" : "Top level resource MUST have @context",
    "okay" : 0
  }
}
~~~

### Batch Update

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/batch_update.action` | `[{JSON}]` | 200: "[header.Location]" New state `[{JSON}]`

The array of JSON objects passed in will be updated in the
order submitted and the response will have the URI of the
resource or an error message in the body as an array in the
same order. __rerum, @id and ObjectID updates are ignored.

## PATCH

### Add Properties

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/patch_set.action` | `{JSON}` | 200: `header.Location` New state `[{JSON}]`
| | | 404: "Object not in RERUM."

A single object is updated by adding all properties in the JSON
payload. If a property already exists, a warning is returned to the user. 
__rerum, @id and ObjectID updates are ignored.

### Remove Properties

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/patch_set.action` | `{JSON}` | 202: `header.Location` New state `[{JSON}]`

A single object is updated by dropping all properties
in the JSON payload list like `key:null`. If a value is included, it must match
to be dropped otherwise an warning is returned to the user
. __rerum, @id and ObjectID updates are ignored.

- **`[{JSON}]`**—Containing various bits of information about the PATCH update.  The object looks like
~~~ (json)
{
  "code" : 200,
  "original_object_id" : "http://devstore.rerum.io/rerumserver/id/5a57a30fe4b09163a80a0a67",
  "new_obj_state" : {
    @id: newID
    .
    .
    .
  },
  "iiif_validation" : {
    "warnings" : [ ],
    "error" : "Top level resource MUST have @context",
    "okay" : 0
  }
}
~~~

## DELETE

Mark an object as deleted. Deleted objects are not included
in query results.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/delete.action` | `String id` or `{JSON}` | 204

There is no batch `DELETE` planned.

## Smart objects

Under Development

## __rerum

Each object has an immutable property called `__rerum` containing a metadata
object about the record retreived, such as it exists at the time.

| Property         | Type      | Description
| ---              | ---       | ---
| history.prime    | String    | The URI of the very top object in this history.
| history.next     | [String]  | An array of URIs for the updated versions of this object. A length > 1 indicates a fork.
| history.previous | String    | The URI of the immediately previous version of this object.
| generatedBy      | String    | Reference to the application whose key was used to commit the object.
| createdAt        | timestamp | Though the object may also assert this about itself, RERUM controls this value.
| isOverwritten    | timestamp | Written when `?overwrite=true` is used. Does not expose the delta, just the update.
| isReleased       | boolean   | Simple reference for queries of the RERUM releasing motivations.
| releases.previous| String    | URI of the released version most recent anscestor in the chain.
| releases.next    | [String]  | Array of URIs for the first `released` decendant in the downstream branches

In the future, this may be encoded as an annotation on the object, using 
existing vocabularies, but for now the applications accessing RERUM will
need to interpret these data if it is relevant.

## REST
The intention of the API is to follow RESTful practices.  To learn more about what it means to be RESTful see http://www.restapitutorial.com/resources.html

## IIIF
RERUM fully supports the IIIF standard and makes third party calls to the IIIF validation API http://iiif.io/api/presentation/validator/service/.  A piece of the response if the validation response of this API so the user knows whether or not their data is following this standard.  Objects that fail IIIF validation are still saved.  

# Web Annotation
RERUM follows the W3C Annotation protocol.  To learn more about Web Annotation see https://www.w3.org/TR/annotation-protocol/

## RERUM Responses
RERUM follows REST, IIIF and Web Annotation standards to form its responses to users.  For more information about why RERUM chose a certain HTTP status code see the graph below.
![httpdd](https://user-images.githubusercontent.com/3287006/32914301-b2fbf798-cada-11e7-9541-a2bee8454c2c.png)
If you are confused as to what type of requests give what reponse, review the [Web Annotation](#web-annotation) and [RESTful](#rest) standards.
