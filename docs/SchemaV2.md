
**JSON Schema for /message (Version 2)**

```json

{
  "title": "A JSON Schema for Message Version 2.0 API.",
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "required": [
    "externalRef",
    "recipient",
    "messageType",
    "subject",
    "content",
    "details"
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
          "description": "cannot be empty"
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
              "type": "string",
               "oneOf": [
                 {
                   "properties": {
                     "sautr": {
                       "type": "string"
                     },
                     "nino": {
                       "type": "string"
                     },
                     "ctutr": {
                       "type": "string"
                     }
                   }
                 }
               ]
            },
            "value": {
              "type": "string"
            }
          }
        },
        "name": {
          "type": "object",
          "required": [
          ],
          "properties": {
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
          },
          "email": {
            "type": "string"
          }
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
      "description": "date in format for LocalDate, yyyy-MM-dd ie 2017-02-14"
    },
    "content": {
      "type": "string",
      "description": "Some base64-encoded HTML"
    },
    "details": {
      "type": "object",
      "required": [
        "formId",
        "statutory",
        "paperSent"
      ],
      "properties": {
        "formId": {
          "type": "string"
        },
        "statutory": {
          "type": "boolean"
        },
        "paperSent": {
          "type": "boolean"
        },
        "sourceData": {
          "type": "string"
        },
        "batchId": {
          "type": "string"
        }
      }
    }
  }
}

```