Performs a XXX request to `url` with the given `body` and `options` and returns the response from the server.

The format of the `body` and `options` parameters is described in the _Request_ section. The return value is
described in the _Response_ section.

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

| Option Name         | Value                                                                                                   | Description                                                                       |
| ------------------- | ------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| basicAuth           | An OBJECT containing `username` and `password` fields                                                   | Set these credentials to perform basic authentication                             |
| proxy               | An OBJECT containing at least a `host` field but optionally `port` or `username` and `password` fields  | Set these options to connect through a proxy                                      |
| timeout             | An ATOMIC that represents the timeout in milliseconds                                                   | Set this option to limit the time a request can take                              |
| headers             | An OBJECT containing the headers                                                                        | Set this option to add custom headers to your request                             |
| insecure            | An ATOMIC *default: false*                                                                              | Set this option to true if you want to skip hostname validation                   |
| enableRedirect      | An ATOMIC *default: true*                                                                               | Set this option to false if you do not want to automatically follow redirects     |
| multipart           | An ATOMIC *default: false*                                                                              | Set this option to true if you want to build a multipart/form-data request        |
| responseContentType | An ATOMIC describing a content type                                                                     | Set this option to override the response type auto detector                       |
| logging             | An ATOMIC (debug, info, warn or error) *default: null*                                                  | Set this option to enable logging of requests and responses                       |

### Multipart
To make multipart requests you simply set the multipart option to `true`
and conform to the multipart body syntax:

    {
        "[PART NAME]": {
            "type": "file|stream|text",
            "contentType": "RFC 822 Content Type",
            "content": "SEE BELOW"
        }
    }

or a LIST in the following format:

    [
        {
            "name":"[PART NAME]",
            "type":"file|stream|text",
            "contentType":"RFC 822 Content Type"
            "content":"SEE BELOW",
        }
    ]

The two bodies above are equivalent. Passing the body as a LIST allows for multiple parts with the same name.

The body can contain multiple parts containing at least the name, content and type options. The contentType option is optional.
See the table below for an explanation of the options which a body part should consist of.

| Option Name            | Value                                                                                                                                                                            | Description                                                                    |
| ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| name                   | An ATOMIC containing the part name                                                                                                                                               | The part name                                                                  |
| content                | An ATOMIC containing a file path or a stream, depending on the type option                                                                                                       | The content of the body part                                                   |
| type                   | Can be `"text"`, `"file"` or `"stream"`, see below for more information                                                                                                          | Determines the type of the content option                                      |
| contentType (optional) | An ATOMIC containing a [RFC 822](https://www.w3.org/Protocols/rfc1341/4_Content-Type.html) content type, or a default depending on the content option's type if not specified    | The content type of the body part's content, this option will not be validated |

The field `type` instructs the request how to interpret the provided content.

| Type field | Content field                                                                                                          |
| ---------- | ---------------------------------------------------------------------------------------------------------------------- |
| file       | A file path. A file patch will be made with the target file as a payload.                                              |
| stream     | A (binary) stream will be expected and consumed. Note that retries are not possible when providing streams as content. |
| text       | Plain text will be sent as the body. If you provide a stream it will be read as text.                                  |

## Response
If the request is performed without errors the return value will be an 
object that describes the response.

### Status
The `status` field contains an object that has a `code` field that 
contains the HTTP Status Code and a `phrase` field that contains the 
description.

    {
        "status": {
            "code": 200,
            "phrase": "OK"
        },
        ...
    }
    
### Headers
The `headers` field contains an object that has the header names as keys 
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
The `version` field contains a string describing the current HTTP 
protocol version.

    {
        "version": "HTTP/1.1",
        ...
    }

### Body
The `body` field contains the response body. The type of content in here 
is auto detected form the Content-Type header which can be overridden
 using the `responseContentType` option.

| Content-Type    | Result                                         |
| --------------- | ---------------------------------------------- |
| Contains "json" | The JSON parsed to an expression               |
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
