
**JSON Schema for /message (Version 3)**

```json

{
  "title": "A JSON Schema for Message Version 3.0 API.",
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "required": [
    "externalRef",
    "recipient",
    "messageType",
    "subject",
    "content"
  ],
  "properties": {
    "externalRef": {
      "type": "object",
      "required": [
         "id",
         "source"
      ],
      "properties": {
        "id": {
          "type": "string",
          "description": "cannot be empty"
        },
        "source": {
          "type": "string",
          "oneOf": [
            {
              "properties": {
                "gmc": {
                  "type": "string"
                },
                "mdtp": {
                  "type": "string"
                }
              }
            }
          ]
        }
      }
    },
    "recipient": {
      "type": "object",
      "required": [
        "taxIdentifier"
      ],
      "properties": {
        "taxIdentifier": {
          "type": "object",
          "required": [
            "name",
            "value"
          ],
          "properties": {
            "name": {
              "type": "string"
            },
            "value": {
              "type": "string"
            }
          }
        },
        "name": {
          "type": "object",
          "required": [
            "line1"
          ],
          "properties": {
            "line1": {
              "type": "string"
            },
            "line2": {
              "type": "string"
            },
            "line3": {
              "type": "string"
            },
            "title": {
              "type": "string"
            },
            "forename": {
              "type": "string"
            },
            "secondForename": {
              "type": "string"
            },
            "surname": {
              "type": "string"
            },
            "honours": {
              "type": "string"
            }
          }
        },
        "email": {
          "type": "string",
          "description": "required if email not found in preference"
        },
        "regime": {
          "type": "string",
          "oneOf": [
            {
              "properties": {
                "paye": {
                  "type": "string"
                },
                "sa": {
                  "type": "string"
                },
                "ct": {
                  "type": "string"
                },
                "fhdds": {
                  "type": "string"
                },
                "vat": {
                  "type": "string"
                },
                "epaye": {
                  "type": "string"
                },
                "sdil": {
                  "type": "string"
                }
              }
            }
          ]
        }
      }
    },
    "messageType": {
      "type": "string"
    },
    "subject": {
      "type": "string"
    },
    "validFrom": {
      "type": "string",
      "pattern": "[0-9]{4}-[0-9]{2}-[0-9]{2}",
      "description": "date in format for LocalDate, yyyy-MM-dd ie 2017-02-14"
    },
    "alertQueue": {
      "type": "string",
       "oneOf": [
         {
           "properties": {
             "BACKGROUND": {
               "type": "string"
             },
             "DEFAULT": {
                "type": "string"
             },
             "PRIORITY": {
               "type": "string"
             }
           }
         }
       ]
    },
    "emailAlertEventUrl": {
      "type": "string"
    },

    "content": {
      "type": "string",
      "description": "Some base64-encoded HTML"
    },
    "details": {
      "description": "required for externalRef.source == 'gmc' ",
      "type": "object",
      "required": [
        "formId"
      ],
      "properties": {
        "formId": {
          "type": "string"
        },
        "issueDate": {
          "type": "string",
          "pattern": "[0-9]{4}-[0-9]{2}-[0-9]{2}",
          "description": "format yyyy-MM-dd ie 2017-02-14, defaults to time api msg received"
        },
        "batchId": {
          "type": "string"
        },
        "sourceData": {
          "type": "string",
          "description": "base64 encoded. Required if 'details' is provided"
        },
        "replyTo": {
          "type": "string",
          "description": "mongoid of parent to this message"
        },
        "threadId": {
          "type": "string",
          "description": "UUID for a  conversation"
        },
        "enquiryType": {
          "type": "string",
          "description": "base64 encoded. Required if 'details' is provided"
        },
        "adviser": {
          "type": "string",
          "description": "PID of the advisor replying to 2wsm"
        },
        "waitTime": {
          "type": "string",
          "description": "response time of 2wsm"
        },
        "topic": {
          "type": "string",
          "description": "2wsm topic"
        }
      }
    }
  }
}

```
