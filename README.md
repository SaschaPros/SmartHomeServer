
# Smarthome Server

This Node.JS server is providing several REST-APIs which can be useful within a smart home system.

## How to use

To use this server you can easily clone the sources and build a Docker image. The server runs without any configuration needed.
A ready-to-go image is also provided on [Docker Hub](https://hub.docker.com/r/spro93/smarthome)

To run the server in Docker use this command:

    docker run -d -p 3000:3000 --name smarthomeserver --expose 3000 spro93/smarthome
To pull the Docker image from Docker Hub use this command:

    docker pull spro93/smarthome

Once the server is running it is reachable on your host with port 3000 by default. Also if the host system crashes or reboots, the container is started automatically again.

## Endpoints

### Check if electricity price is negative

Fetches current electricity prices at [smartenergy.at](https://apis.smartenergy.at/market/v1/price) and checks if the prices are negative at the moment of the call.
The API used for fetching provides prices of the EPEX Spot market for **Austria** for the next 24 hours every 15 minutes.

**URL** : `/api/electricityPrice?{additionalAmount}`

**Method** : `GET`

##### Parameters

> | name              |  type     | data type      | description                         | default value |
> |-------------------|-----------|----------------------|-------------------------------------|-------------------|
> | `additionalAmount` |  optional | double | Amount which should be added to the prices provided by the API (e.g. to cover additional taxes)  | 0 |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/html; charset=utf-8` | Boolean represented by 0 (**false**) or 1 (**true**) |
> | `500`         | `text/html; charset=utf-8` | String with the error message (e.g. "*additionalAmount is not a number*") |

##### Example cURL

> ```javascript
>  curl 'http://localhost:3000/api/electricityPrice'
>  curl 'http://localhost:3000/api/electricityPrice?additionalAmount=123'
> ```

### Check if given coordinates are exposed by the sun

Calculates based on the given GPS-location and the current date and time if the sun is within the given coordinates (azimuth and altitude).
For the calculation suncalc3 is used: 

NPM: https://www.npmjs.com/package/suncalc3
GitHub: https://github.com/hypnos3/suncalc3

**URL** : `/api/isExposedToSun?{latitude}&{longitude}&{minAzimuth}&{maxAzimuth}&{minAltitude}&{maxAltitude}`

**Method** : `GET`

##### Parameters

> | name              |  type     | data type      | description                         | default value |
> |-------------------|-----------|----------------------|-------------------------------------|-------------------|
> | `latitude` |  optional | double | GPS-latitude of the requestes location | 0 |
> | `longitude` |  optional | double | GPS-longitudeof the requestes location | 0 |
> | `minAzimuth` |  **required** | double | Minimal azimuth degrees|  |
> | `maxAzimuth` |  **required**| double | Maximum azimuth degrees |  |
> | `minAltitude` |  optional | double | Minimal altitude degrees | 0 |
> | `maxAltitude` |  optional | double | Maximum altitude degrees | 90 |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/html; charset=utf-8` | Boolean represented by 0 (**false**) or 1 (**true**) |
> | `500`         | `text/html; charset=utf-8` | String with the error message (only if input validation fails) |

##### Example cURL

> ```javascript
>  curl 'http://localhost:3000/api/isExposedToSun?minAzimuth=225&maxAzimuth=40'
>  curl 'http://localhost:3000/api/isExposedToSun?minAzimuth=225&maxAzimuth=40&minAltitude=10'
>  curl 'http://localhost:3000/api/isExposedToSun?latitude=48.0676871028563&longitude=12.862135153533522&minAzimuth=225&maxAzimuth=40&minAltitude=10&maxAltitude=80'
> ```
