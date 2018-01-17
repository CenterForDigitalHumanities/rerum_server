# API (0.9)
- [API](#api)
    - [GET](#get)
        - [by ID](#single-object-by-id)
        - [history parents](#history-tree-before-id)
        - [history children](#history-tree-since-id)
        - [by custom query](#by-custom-query)
    - [POST](#post)
        - [Create](#create)
        - [Batch Create](#batch-create)
    - [PUT](#put)
        - [Update](#put-update)
        - [Batch Update](#batch-update)
    - [PATCH](#patch)
        - [Release](#rerum-released)
        - [Retract](#rerum-released)
        - [Update](#patch-update)
        - [Set](#add-properties)
        - [Unset](#remove-properties)
    - [DELETE](#delete)
        - [Delete](#delete)
    - [Smart objects](#smart-objects)
    - [__rerum](#__rerum)
    - [RERUM @context](#rerum-context)
    - [RERUM history](#rerum-history)
    - [RERUM attribution](#rerum-attribution)
    - [RERUM authentication](#rerum-authentication)
    - [REST](#rest)
    - [IIIF](#iiif)
    - [Web Annotation](#web-annotation)
    - [Error Responses](#rerum-responses)

All the following interactions will take place between
the server running RERUM and the application server. If
you prefer to use the public RERUM server (which I hope
you do), the base URL is `http://rerum.io/rerumserver`. 

## GET

### Single object by id

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/id/_id` | `empty` | 200: `{JSON}`

- **`_id`**—the id of the object in RERUM.

Call over HTTP can be made through GET request to their
unique URL Ex. http://rerum.io/rerumserver/id/aee33434bbc333444ff

### History tree before ID

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/history/_id` | `empty` | 200: `[{JSON}]`

- **`_id`**—the id of the object in RERUM.
- **`[{JSON}]`**—an array of the resolved objects of all parent history objects

As objects in RERUM are altered, their previous state is saved through
a history tree.  Users can ask for all parent versions of a given object.
Call over HTTP can be made through GET request to their
unique URL Ex. http://rerum.io/rerumserver/history/aee33434bbc333444ff

### History tree since ID

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/since/_id` | `empty` | 200: `[{JSON}]`

- **`_id`**—the id of the object in RERUM.
- **`[{JSON}]`**—an array of the resolved objects of all child history objects

As objects in RERUM are altered, their previous state is saved through
a history tree.  You can ask for all child versions of a given object.
Call over HTTP can be made through GET request to their
unique URL Ex. http://rerum.io/rerumserver/since/aee33434bbc333444ff

### By custom query

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/getByProperties.action` | `{JSON}` | 200: `[{JSON}]`

- **`{JSON}`**—the properties in JSON format for the query
- **`[{JSON}]`**—an array of the resolved objects of all objects that match the query

The bulk of any application's interactions with RERUM will be
in the queries. This simple format will be made more complex
in the future, but should serve the basic needs as it is.
All responses are in a JSON Array, even if zero records or a single
record is returned.  RERUM will test for property matches, 
so `{ "@type" : "sc:Canvas", "label" : "page 46" }` will match

~~~ (json)
[{
  "@id": "https://rerum.io/rerumserver/id/ae33ffee5656789",
  "otherContent": [],
  "label": "page 46",
  "width": 730,
  "images": [],
  "height": 1000,
  "@type": "sc:Canvas"
}]
~~~

## POST

### Create

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/create.action` | `{JSON}` | 201: `header.Location` "Created @ `[@id]` `{JSON}`

- **`{JSON}`**—The object to create
- **`{JSON}`**—Containing various bits of information about the create.  The object looks like

Add a completely new object to RERUM and receive the location
in response.  Accepts only single JSON objects for RERUM storage. 
Mints a new URI and returns the object's location as a header. 
If the object already contains an `@id` that matches an object in RERUM,
the API will direct the user to use [update](#update) instead.

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
| `/v1/batchCreate.action` | `[{JSON}]` | 200: `[{JSON}]`

- **`[{JSON}]`**—an array of objects to create in RERUM
- **`[{JSON}]`**—an array of the resolved objects from the creation process

The array of JSON objects passed in will be created in the
order submitted and the response will have the URI of the new
resource or an error message in the body as an array in the
same order.  When errors are encountered, the batch process
will attempt to continue for all submitted items.

## PUT

### PUT Update

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/put_update.action` | `{JSON}` | 200: `header.Location` New state `{JSON}`

- **`{JSON}`**—The requested new state for the object.
- **`{JSON}`**—Containing various bits of information about the PUT update.  The object looks like

Replace an existing record through reference to its internal
RERUM id.  This will have the effects of set and unset actions.  
New keys will be created and keys not present in the request will be dropped.  
When an object is updated, the `@id` will be changed, as the previous
version will maintain its place in the history of that object.
 __rerum, @id and ObjectID updates are ignored.

~~~ (json)
{
  "code" : 200,
  "original_object_id" : "http://rerum.io/rerumserver/id/5a57a30fe4b09163a80a0a67",
  "new_obj_state" : {
    @id: newID
    .
    .
    .
  },
  "iiif_validation" : {
    "warnings" : ["Array of warnings from IIIF validator"],
    "error" : "Error for why this object failed validation",
    "okay" : 1 // 0 or 1 as to whether or not it passed IIIF validation
  }
}
~~~

### Batch Update

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/batch_update.action` | `[{JSON}]` | 200: "[header.Location]" New state `[{JSON}]`

- **`[{JSON}]`**—an array of objects to update in RERUM.  Each object MUST contain the @id.
- **`[{JSON}]`**—an array of the resolved objects in their new state from the update process

The array of JSON objects passed in will be updated in the
order submitted and the response will have the URI of the
resource or an error message in the body as an array in the
same order. __rerum, @id and ObjectID updates are ignored.

## PATCH

### RERUM released

**Under Development**

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/release.action` | `String @id` or `{JSON}` | 200: `header.Location` New state `{JSON}`
| `/v1/retract.action` | `String @id` or `{JSON}` | 200: `header.Location` New state `{JSON}`

- **`String @id`**—The @id of the object.
- **`{JSON}`**—The object.  Must contain @id. 

RERUM allows for an object to move back and forth between a released and unreleased state.  Objects in released states are locked such that any action that may change the object is refused.  Calling any update or delete action on a released object will result in an error response. The release and retract actions will perform an update to the __rerum.isReleased and __rerum.releases properties of the object.

### Patch Update

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/patch_update.action` | `{JSON}` | 200: `header.Location` New state `{JSON}`

- **`{JSON}`**—The requested new state for the object.  MUST contain the @id
- **`{JSON}`**—Containing various bits of information about the PATCH update.

A single object is updated by altering the set or subset of properties in the JSON
payload. If a property submitted in the payload does not exist, an error will be returned to the user. If
`key:null` is submitted, the key will not be removed.  Instead, the value will be null. 
Properties not submitted in the payload object will go unaltered.  If a new key is submitted, the set action will not
be performed.  Instead, an error will be returned as this method only updates existing keys. 
__rerum, @id and ObjectID updates are ignored.

### Add Properties

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/patch_set.action` | `{JSON}` | 200: `header.Location` New state `{JSON}`

- **`{JSON}`**—The requested new state for the object. MUST contain the @id
- **`{JSON}`**—Containing various bits of information about the PATCH update.

A single object is updated by adding all properties in the JSON
payload. If a property already exists, a warning is returned to the user. 
__rerum, @id and ObjectID updates are ignored.

### Remove Properties

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/patch_set.action` | `{JSON}` | 202: `header.Location` New state `{JSON}`

- **`{JSON}`**—The requested new state for the object.  Must contain the @id.
- **`{JSON}`**—Containing various bits of information about the PATCH update.

A single object is updated by dropping all properties
in the JSON payload list like `key:null`. If a value is included, it must match
to be dropped otherwise an warning is returned to the user
. __rerum, @id and ObjectID updates are ignored.

- **`{JSON}`**—Containing various bits of information about any PATCH update action.  The object looks like:

~~~ (json)
{
  "code" : 200,
  "original_object_id" : "http://rerum.io/rerumserver/id/5a57a30fe4b09163a80a0a67",
  "new_obj_state" : {
    @id: newID
    .
    .
    .
  },
 "iiif_validation" : {
    "warnings" : ["Array of warnings from IIIF validator"],
    "error" : "Error for why this object failed validation",
    "okay" : 1 // 0 or 1 as to whether or not it passed IIIF validation
  }
}
~~~

## DELETE

Requests can be made by the string @id or a JSON object containsing the {@id:id}.  
RERUM DELETE will not remove anything from the server. Deleted objects are only marked as deleted.
Objects marked as deleted do not return in query results except queries by @id.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/v1/delete.action` | `String @id` or `{JSON}` | 204

- **`String @id`**—The @id of the object.
- **`{JSON}`**—The object to delete.  Must contain @id. 

There is no batch `DELETE` planned. 

A deleted object looks like
~~~ (json)
{
  "@id" : "http://rerum.io/rerumserver/id/5a57a30fe4b09163a80a0a67",
  "__deleted" : {
    "object" : {
      "@id" : "http://rerum.io/rerumserver/id/5a57a30fe4b09163a80a0a67",
      .
      .
      .
    },
    "deletor" : "TODO",
    "time" : 1516213216852
  }
}
~~~
Note: The object as it existed at the time of deletion exists in __deleted.object, which includes the history.

## Smart objects

`Under Development`

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
| isOverwritten    | timestamp | Written when PUT update is used. Does not expose the delta, just the update.
| isReleased       | boolean   | Simple reference for queries of the RERUM releasing motivations.
| releases.previous| String    | URI of the released version most recent anscestor in the chain.
| releases.next    | [String]  | Array of URIs for the first `released` decendant in the downstream branches

In the future, this may be encoded as an annotation on the object, using 
existing vocabularies, but for now the applications accessing RERUM will
need to interpret these data if it is relevant.

### RERUM history

History is stored through pointers that create a B-Tree.  All nodes in the B-tree know the root node, the previous node and the next node.  

You can ask for all descendants or all ancestors from any given node so long as you know the node's @id and the node is not deleted.  See [history parents](#history-tree-before-id) and [history children](#history-tree-since-id) for more details about this process.

Deleted objects are not present in the B-Tree, but do exist as separate nodes that can be requested by the URI directly.  A reference to their previous and next nodes as they were at the time of deletion exists in these deleted nodes.  

### RERUM attribution

**Undergoing development**
RERUM will associate your key with any action you perform on an item in `__rerum.generatedBy`.  A key match is required for certain actions so that not just anyone can alter or remove the things you have created.  If an authorized user attempts one of these actions they will get a 403:Forbidden response with explanation on how to fork your work instead of altering it.  

### RERUM authentication

**Undergoing development**
RERUM will create an `agent` for you when you register to use it.  This `agent` will be in JSON format and store a key generated by [Auth 0](https://auth0.com/).  The key will remain unique to your application on your server and will be used to generate a session.  The session will expire and can be forced to expire (like a logout).  Expired sessions will cause 401:Unauthorized response with explanation on how to refresh your session or register your application.

### RERUM context

**Undergoing development**
Objects in RERUM should be JSON-LD, which means they should have an @context provided when they are created.  However, ordinary JSON documents are allowed in the store.  These JSON documents can be interpreted as JSON-LD by referencing a JSON-LD context document in an HTTP Link Header as described here https://www.w3.org/TR/json-ld/#h3_interpreting-json-as-json-ld.  RERUM provides this @context in the Link header and also provides an @context for the `__rerum` terms mentioned above.  

## REST

The intention of the API is to follow RESTful practices.  These practices drive what requests we accept and what responses we have to the various scenarios around those requests.  To learn more about what it means to be RESTful see http://www.restapitutorial.com/resources.html

## IIIF

RERUM fully supports the IIIF standard and makes third party calls to the IIIF validation API http://iiif.io/api/presentation/validator/service/.  A piece of the RERUM response is the validation response of this API so the user knows whether or not their data is following this standard.  Objects that fail IIIF validation are still saved.  

## Web Annotation

RERUM follows the W3C Annotation protocol.  To learn more about Web Annotation see https://www.w3.org/TR/annotation-protocol/

## RERUM Responses

RERUM follows REST, IIIF and Web Annotation standards to form its responses to users.  For more information about why RERUM chose a certain HTTP status code see the graph below.
![httpdd](https://user-images.githubusercontent.com/3287006/32914301-b2fbf798-cada-11e7-9541-a2bee8454c2c.png)
If you are confused as to what type of requests give what reponse, review the [Web Annotation](#web-annotation) and [RESTful](#rest) standards.
