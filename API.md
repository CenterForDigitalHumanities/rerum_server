# API (0.0.1)

- [API](#api)
    - [GET](#get)
        - [GET by ID](#single-object-by-id)
            - [Collection Aliases (case insensitive)](#collection-aliases-case-insensitive)
        - [GET by custom query](#get-by-custom-query)
    - [POST](#post)
        - [Queries](#queries)
        - [Create](#create)
        - [Batch Create](#batch-create)
    - [PUT](#put)
        - [Update](#update)
        - [Batch Update](#batch-update)
    - [PATCH]
      - [Update](#patch-update)
      - [Set/Unset](#patch-set)
    - [DELETE](#delete)
    - [Smart objects](#smart-objects)
    - [__rerum](#__rerum)
    - [REST](#rest)
    - [IIIF](#iiif)
    - [Web Annotation](#webanno)
    - [Error Responses](#responses)

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

#### GET by custom query

The bulk of any application's interactions with RERUM will be
in the queries. This simple format will be made more complex
in the future, but should serve the basic needs as it is.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/getByProperties.action` | `{JSON}` | 200: JSON [obj]
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
| `/create.action` | `{JSON}` | 201: `header.Location` "Created @ `[@id]` `[{JSON}]`

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
| `/batchCreate.action` | `[{JSON}]` | 200: "`[@id]`" 

The array of JSON objects passed in will be created in the
order submitted and the response will have the URI of the new
resource or an error message in the body as an array in the
same order.

When errors are encountered, the batch process
will attempt to continue for all submitted items.

## PUT

### Update

Update an existing record through reference to its internal
RERUM id.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/res/:id` | `{JSON}` | 202: `header.Location` "Updated `[@id]`
| | | 400: "Unknown property."
| | | 404: "No record found."

A single object is updated with all properties in the
JSON payload. Unnamed properties are not affected. Unknown
properties throw 400 (use [set](#add-properties)). `@type` will not be normalized
in storage and `@context` for [known types](#collection-aliases)
are filled upon delivery and may be omitted.

When an object is updated, the `@id` will be changed, as the previous
version will maintain its place in the history of that object. To overwrite
the same object, instead of creating a new version, include `?overwrite=true`
in the request. See [Friendly Practices](practices.md) for the rare times when
creating a new entry in the history is not wanted and [Versioning](version.md)
for an explanation of how each object's history is maintained in RERUM.

### Add Properties

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/set/:id` | `{JSON}` | 202: `header.Location` "Updated `[@id]`
| | | 404: "No record found."

A single object is updated by adding all properties in the JSON
payload. If a property already exists, it is overwritten without
feedback.

### Remove Properties

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/unset/:id` | `{JSON}` | 202: `header.Location` "Updated `[@id]`
| | | 404: "No record found."

A single object is updated by dropping all properties
in the JSON payload. If a value is included, it must match
to be dropped.

### Batch Update

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `/[res,set,unset]/:id` | `[{JSON}]` | 202: "`[@id]`"

The array of JSON objects passed in will be updated in the
order submitted and the response will have the URI of the
resource or an error message in the body as an array in the
same order. [Smart objects](#smart-objects) will be handled a
little differently. Batch updating has no equivalent `overwrite`
parameter.

The request path will indicate the action and possible errors.
The response body will include status and errors as above for
201, 202, 400, etc., but the detail will be much less than a
single request. When errors are encountered, the batch process
will attempt to continue for all submitted items.

## DELETE

Mark an object as deleted. Deleted objects are not included
in query results.

| Patterns | Payloads | Responses
| ---     | ---     | ---
| `res/:id` | `String` | 204
| | | 404: "No record found."

There is no batch `DELETE` planned.

## Smart objects

Known things in RERUM gain superpowers to save the embedded
items and update batches cleverly. To trigger this behavior,
add `?recursive=true` to create or update requests. GET the
new object from the location returned to learn the new URIs
assigned to the embedded entities.

| Object | Behavior
| ---     | ---     | ---
| Manifest  | Also create or update any Canvases, AnnotationLists, or Annotations within.
| Canvas    | Also create or update any AnnotationLists or Annotations within.
| AnnotationLists | Also create or update Annotations within.

In fact, the recursive flag will tell RERUM to traverse any object and look
for these known types to also update, but the standard structure of IIIF
objects means that the action will be much more reliable.

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
| releases.previous|  String   | URI of the released version most recent anscestor in the chain.
| releases.next    | [String]  | Array of URIs for the first `released` decendant in the downstream branches

In the future, this may be encoded as an annotation on the object, using 
existing vocabularies, but for now the applications accessing RERUM will
need to interpret these data if it is relevant.

[home](index.md) | [Friendly Practices](practices.md) | [API](api.md) | [Register](register.md)
