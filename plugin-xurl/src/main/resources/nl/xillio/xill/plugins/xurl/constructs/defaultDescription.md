Perform a XXXX request and return the response from the server.

## Request
### Body
The body parameter can contain the following types:

| Input                            | Default Content-Type     |
| -------------------------------- | ------------------------ |
| Stream                           | application/octet-stream |
| LIST                             | application/json         |
| OBJECT                           | application/json         |
| Result from XML plugin           | application/xml          |
| OBJECT with the multipart option | multipart/form-data      |
| ATOMIC                           | text/plain               |

However, you can influence the content type by manually setting the 
Content-Type header.

### Options
To set various options for the request you can use the options parameter. 
The passed value should be an object containing
option names as keys and their respective values as values.
Check out the code examples for more information about the options.

| Option Name         | Value                                                                             | Description                                                                       |
| ------------------- | --------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| basicAuth           | An object containing a username and a password                                    | Set these credentials to perform basic authentication                             |
| proxy               | An object contains at least a host but optionally a port or username and password | Set these options to connect through a proxy                                      |
| timeout             | An integer that represents the timeout in milliseconds                            | Set this option to limit the time a request can take                              |
| headers             | An object containing the headers                                                  | Set this option to add custom headers to your request                             |
| insecure            | A boolean *default: false*                                                        | Set this option to true if you want to skip hostname validation                   |
| multipart           | A boolean *default: false*                                                        | Set this option to true if you want to build a multipart/form-data request        |
| responseContentType | A String describing a content type                                                | Set this option to override the the response type auto detector                   |
| logging             | A String (debug, info, warn or error) *default: none*                             | Set this option to enable logging of requests and responses                       |

### Multipart
To make multipart requests you simply set the multipart option to *true* 
and conform to the multipart body syntax:

    {
        "[PART NAME]": {
            "type": "file|stream|text",
            "contentType": "RFC 822 Content Type",
            "content": "SEE BELOW"
        }
    }

Read more about the [RFC 822](https://www.w3.org/Protocols/rfc1341/4_Content-Type.html).

The field *type* instructs the request how to interpret the provided content.

| Type field | Content field                                                                                                          |
| ---------- | ---------------------------------------------------------------------------------------------------------------------- |
| file       | A file path. A file post will be made with the target file as a payload.                                               |
| stream     | A (binary) stream will be expected and consumed. Note that retries are not possible when providing streams as content. |
| text       | Plain text will be sent as the body. If you provide a stream it will be read as text.                                  | 

## Response
If the request is performed without errors the return value will be an 
object that describes the response.

### Status
The *status* field contains an object that has a *code* field that 
contains the HTTP Status Code and a *phrase* field that contains the 
description.

    {
        "status": {
            "code": 200,
            "phrase": "OK"
        },
        ...
    }
    
### Headers
The *headers* field contains an object that has the header names as keys 
and header values as values.

    {
        "headers" : {
            "Content-Type" : "application/json; charset=UTF-8",
            "access-control-allow-origin" : "*",
            "Vary" : "Accept-Encoding",
            "Date" : "Thu, 18 Feb 2016 09:10:46 GMT",
            "Server" : "Google Frontend",
            "Cache-Control" : "private",
            "Transfer-Encoding" : "chunked"
        },
        ...
    }

### Version
The *version* field contains a string describing the current HTTP 
protocol version.

    {
        "version": "HTTP/1.1",
        ...
    }

### Body
The *body* field contains the response body. The type of content in here 
is auto detected form the Content-Type header which can be overridden
 using the *responseContentType* option.

| Content-Type    | Result                                         |
| --------------- | ---------------------------------------------- |
| Contains "json" | The json parsed to an expression               |
| Contains "xml"  | An XML node that can be used by the XML plugin |
| Contains "text" | A plain text string                            |
| Otherwise       | A stream containing the data                   |

    // JSON
    {
        "body": {
            "data": ["It's, "a", "good", "day"]
        },
        ...
    }
    // XML
    {
        "body": "XML Document[first node = Data]",
        ...
    }
    // TEXT
    {
        "body": "Hello World",
        ...
    )
    // STREAM
    {
        "body": "[Stream application/octet-stream]",
        ...
    }