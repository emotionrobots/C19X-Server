# C19X-Server

### Decentralised COVID-19 contact tracing.
**Private. Simple. Secure.**

C19X iOS and Android app clients communicate with this central server via HTTPS to:
1. Register device : `GET /registration`
   - Obtains a globally unique serial number and shared secret on first run.
2. Synchronise time : `GET /time`
   - Get server time to coarsely synchronise device and server time.
   - Password-less authentication is based on a function of shared secret and time.
3. Submit status : `POST /status?key=serialNumber&value=encryptedMessage`
   - Send health status update (normal / symptoms / diagnosis) to server.
   - Encrypted message contains status, time and abstracted contact pattern.
   - Message encrypted with shared secret, thus correctly decrypted time authenticates message.
   - Contact pattern is the number of contacts per signal strength over retention period.
   - Contact pattern derived from maximum signal strength detected per quantized time period.
   - Contact pattern data is used to optimise infection risk analysis parameters.
4. Get message : `GET /message?key=serialNumber&value=encryptedMessage`
   - Get device specific text message from server, e.g. to provide additional information.
   - Offers a mechanism for loose integration with other systems via user controlled air gap.
   - Example "Please get in touch to provide contact information and obtain a test kit".
5. Get infection data : `GET /infectionData`
   - Get infection data for on-device matching.
   - This is a table of beacon code seeds for regenerating the beacon codes associated with infection reports.
   - Beacon code seeds cannot be traced back to day codes, shared secret or serial number.
   - Day codes, beacon code seeds and beacon codes are generated via one-way functions and lossy transformations.
   - Infection data is downloaded by app clients (not pushed) on a daily basis.
6. Get parameters : `GET /parameters`
   - Get all parameters such as server address, retention period, and matching criteria.
   - Parameter updates on the server (config/parameters.json) are automatically published.
   - Parameters are downloaded by app clients (not pushed) on a daily basis. 
   
The central server also offers:
1. Embedded web server
   - For publishing an associated web site (e.g. https://www.c19x.org)
2. Administration functions
   - Update infection data immediately.
   - Set health status associated with serial number, e.g. for integration with test results database.
   - Set message associated with serial number, e.g. to request additional action from user.
   - List health status and contact patterns associated with all serial numbers, e.g. for integration with analysis systems.

## Building the code

1. Install the latest OpenJDK and Eclipse IDE on Ubuntu Linux
2. Clone the repository
3. Build and run project (see org.c19x.server.C19XHttpsServer.main() for instructions)

## Testing the server

1. Start HTTPS server.
2. Call `GET /time` to confirm connection, should return a plain text long number.

