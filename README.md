# eth-hot-wallet
Spring Boot based hot ETH wallet (non-multisiged). Support both on-premise and on-cloud. It can be integrated into existing system to support ETH payment/transaction. Multiple receiving address is supported through forwarder contract.

## Setup Development Environment
1. Install PostgreSQL and Java 8.
2. Create a new role. user name is "wallet", password is "password". Set the privileges to yes for all.
3. Create a new DB, name is "ethHotWallet", set the owner to the newly created role.
4. Run eth-hot-wallet-1.0.0-SNAPSHOT.jar by using "java -jar eth-hot-wallet-1.0.0-DEV.jar", Kovan testnet will be used by default. If you want to run it on ETH mainnet, please use prod profile.
5. If you want to run it from eclipse or IntelliJ, sync the latest code, import the project and add btc-core-1.0.0.jar to local maven repo.

Note: If you have problem about eth-core.jar in the pom, please add it to your maven repo manually.

## REST API 

- **Create wallet:  POST** https://hostname:7100/api/v1/eth/wallet/new

    wallet-per-user is supported. You can create one or multiple wallets for one user.
  
  example input:
  ```javascript
  {
  	"symbol": "ETH",
  	"label":"test wallet 001",
  	"enabled":true
  }
  ```
  
  example output:
  ```javascript
  {
    "id": "c45812ee95a24e0fa4c2b06281dd4248",
    "label": "test wallet 001",
    "enabled": true,
    "address": "0x8a54d4afe8b9b1e57ae81dd2295f99f0d539877d",
    "created_date": "2020-06-11T17:17:42.211Z"
  }
  ```

  
 - **Get wallet:  GET**  https://hostname:7100/api/v1/eth/wallet/{walletId}

    output example:
    ```javascript
        {
          "id": "bef1a9a4e39e4cb8b36be1ff9681529d",
          "enabled": true,
          "createdDate": "2020-06-01T21:03:38.290Z",
          "label": "test wallet 001",
          "balance": {
            "estimated": "0.00754024",
            "available": "0.00754024"
          }
        }
    ```

- **Generate receiving address:  POST**   https://hostname:7100/api/v1/eth/wallet/{walletId}/address/new

    example input:
    ```javascript
    {
      "symbol":"eth",
      "label":"test wallet 001"
    }
    ```
  
    A new contract will be deployed and this contract will forward all token to owner account. 
    example output:
    ```javascript
    {
      "id": "bc80e15eb8594101a661bab7aa7aa489"
    }
    ```

- **Get Balance:  GET**   https://hostname:7100/api/v1/eth/wallet/{walletId}}/balances

    example output:
    ```javascript
    {
      "balances": [
        {
          "symbol": "ETH",
          "balance": 909289000000000000
        }
      ]
    }
    ```

- **Send coin directly:   POST**   https://hostname:7100/api/v1/eth/wallet/{walletId}/send

   example input:
    
   ```javascript
      {
        "symbol":"eth",
        "address": "0x51574e65ac50F6B42eC65DEF4a85a082c0086dD5",
        "amount": "10000000000000000"
      }
   ```
     
   example output:
   
   ```javascript
      {
        "transaction_id": "0xf26c228984b7eca9de94fc409dd0e7e5c3ee1deea06ae83752cb71cea8ecc82f"
      }
   ```
