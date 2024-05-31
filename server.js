const express = require('express');
const app = express();
const mysql = require('mysql');
const session = require('express-session');
const bcrypt = require('bcrypt');
const bodyParser = require('body-parser');
const crypto = require('crypto');
const {Web3} = require('web3');
const fs = require('fs');
const contractJson = require('./truffle/build/contracts/Authentifi.json');
const {abiArray, contractByteCode} = require("./abiArray");
// Secret ID for session
const secret_id = process.env.secret;

// Salt for hashing
const saltRounds = 10;

// IP and port
const IP = 'localhost';
const port = process.env.PORT || 8080;

// View engine
// app.set('views', path.join(__dirname, 'views'));
// app.set('view engine', 'ejs');

// Body-parser Middleware
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended: true
}));

// Express session Middleware
// app.use(session({
//     secret: secret_id,
//     saveUninitialized: true,
//     resave: true
// }));

// MySQL Connection
const connection = mysql.createConnection({
    host: IP,
	port: 8889,
    user: process.env.database_user||"root",
    password: process.env.database_password||"root",
    database: 'authentifi'
});

connection.connect(function(err) {
    if (!err) {
        console.log('Connected to MySql!\n');
    } else {
        console.log('Not connected to MySql.\n', err);
		process.exit();
    }
});

// Web3 connection
const web3 = new Web3(new Web3.providers.HttpProvider('http://127.0.0.1:7545'));
console.log(`Talking with a geth server \n`);



const address = '0x48FC9c5a54BC77138E5F15A8ea14f0ea126Bd9E7';

const contractInstance = new web3.eth.Contract(contractJson.abi,address);

// This function generates a QR code
function generateQRCode() {
    return crypto.randomBytes(20).toString('hex');
}

// Hash password using bcrypt
function hashBcrypt(password) {
    return bcrypt.hashSync(password, saltRounds);
}

// Hash email using md5
function hashMD5(email) {
    return crypto.createHash('md5').update(email).digest('hex');
}

// Routes for webpages
app.use(express.static(__dirname + '/views'));
app.use(express.static(__dirname + '/views/davidshimjs-qrcodejs-04f46c6'));

// Manufacturer generates a QR Code here
app.get('/createCodes', (req, res) => {
    res.sendFile('views/createCodes.html', { root: __dirname });
});

// Creating a new retailer
app.get('/createRetailer', (req, res) => {
    res.sendFile('views/createRetailer.html', { root: __dirname });
});

// Main website which has 2 routers - manufacturer & retailer
app.get('/', (req, res) => {
    res.sendFile('views/index.html', { root: __dirname });
});


/**
 * Description: Adds a user to the database and to the blockchain
 * Request:     POST /signUp
 * Send:        JSON object which contains name, email, password, phone
 * Receive:     200 if successful, 400 otherwise
 */
app.post('/signUp', async (req, res) => {
    console.log('Request to /signUp\n');
    let name = req.body.name;
    let email = req.body.email;
    let password = req.body.password;
    let location = req.body.location;
    let phone= req.body.phone;
    let hashedPassword = hashBcrypt(password);
    let hashedEmail = hashMD5(email);

    console.log(`Email: ${email} \n`);
    // Adding the user in MySQL
    connection.query('SELECT * FROM USER WHERE Email = ? LIMIT 1', [email],  async (error, results) => {
        if (error) {
            console.log(error);
            return res.status(400);
        }
        if (results.length) {
            let ok = await createCustomer(hashedEmail, name, location);
            return res.status(200).send('Adding Old Customer from database');
        }

        connection.query('INSERT INTO USER VALUES (?,?,?,?)', [name, phone, hashedPassword, email], async (error, results) => {
            if (error) {
                console.log(error);
                return res.status(400);
            }
            res.status(200).send('Signup successful!');

            let ok = await createCustomer(hashedEmail, name, location);
            if (ok) {
                console.log(`User ${hashedEmail} successfully added to Blockchain!\n`);
            } else {
                console.log('ERROR! User could not be added to Blockchain.\n');
            }
        });
    });
});

// Add the user in Blockchain
async function createCustomer(hashedEmail, name, phone) {
    const accounts  = await web3.eth.getAccounts()
    return contractInstance.methods.createLogistic(hashedEmail, name, phone).send({ from: accounts[0], gas: 3000000 });
}


/**
 * Description: Login the user to the app
 * Request:     POST /login
 * Send:        JSON object which contains email, password
 * Receive:     200 if successful, 400 otherwise
 */
app.post('/login', (req, res) => {
    console.log('Request to /login\n');
    let email = req.body.email;
    let password = req.body.password;
    console.log(`Email:${email} \n`);
    connection.query('SELECT * FROM user WHERE email = ? LIMIT 1', [email], (error, results) => {
        if (error) {
            console.log(error);
            return res.status(400);
        }
        if (results.length) {
            connection.query('SELECT hashedPassword FROM USER WHERE Email = (?)', [email], (error, results) => {
                if (error) {
                    console.log(error);
                    return res.status(400);
                }
                let pass = results[0].hashedPassword;
                if (bcrypt.compareSync(password, pass)) {
                    console.log(`Login successful with ${email} \n`);
                    return res.status(200).send('Login successful!');
                }
                return res.status(400).send('Login failed.');
            });
        }else{
            console.log('Email does not exist!\n');
            return res.status(400).send('Email does not exist!');
        }

    });
});


/**
 * Description: Adds a retailer to the database and to the blockchain
 * Request:     POST /retailerSignUp
 * Send:        JSON object which contains name, email, password, location
 * Receive:     200 if successful, 400 otherwise
 */
app.post('/retailerSignup', async (req, res) => {
    console.log('Request to /retailerSignup\n');
    let retailerEmail = req.body.email;
    let retailerName = req.body.name;
    let retailerLocation = req.body.location;
    let retailerPassword = req.body.password;
    let retailerHashedPassword = hashBcrypt(retailerPassword);
    let retailerHashedEmail = hashMD5(retailerEmail);
    console.log(`retailerEmail: ${retailerEmail}, hashedEmail: ${retailerHashedEmail} \n`);
    // Adding the retailer in MySQL
    connection.query('SELECT * FROM RETAILER WHERE retailerEmail = ? LIMIT 1', [retailerEmail], async (error, results) => {
        if (error) {
            console.log(error);
            return res.status(400).send('Some SQL Error');
        }
        if (results.length) {
            let ok = await  createRetailer(retailerHashedEmail, retailerName, retailerLocation);
            return res.status(200).send('Adding Old Retailer to blockchain successful');
        }
        connection.query('INSERT INTO RETAILER VALUES (?,?,?,?)', [retailerName, retailerEmail, retailerLocation,
                                                                    retailerHashedPassword], async (error, results) => {
            if (error) {
                console.log(error);
                return res.status(400).send('Some SQL Error');
            }
            // Adding retailer to Blockchain
            let ok = await  createRetailer(retailerHashedEmail, retailerName, retailerLocation);
            if (ok) {
                console.log(`Retailer ${retailerHashedEmail} successfully added to Blockchain!\n`);
                return res.status(200).send('Retailer successfully added');
            }
            console.log('ERROR! Retailer could not be added to Blockchain.\n');
            return res.status(400).send('Adding Retailer Unsuccessful');
        });
    });
});

// Add retailer to Blockchain
async function createRetailer(retailerHashedEmail, retailerName, retailerLocation) {
    const accounts = await web3.eth.getAccounts()
    return contractInstance.methods.createRetailer(retailerHashedEmail, retailerName, retailerLocation,
                                       ).send( { from: accounts[0], gas: 3000000 });
}


/**
 * Description: Login the retailer to the app
 * Request:     POST /retailerLogin
 * Send:        JSON object which contains email, password
 * Receive:     200 if successful, 400 otherwise
 */
app.post('/retailerLogin', (req, res) => {
    console.log('Request to /retailerLogin\n');
    let email = req.body.email;
    let retailerPassword = req.body.password;
    console.log(`Email: ${email} \n`);
    connection.query('SELECT * FROM RETAILER LIMIT 1', [], (error, results) => {
        if (error || results.length === 0) {
            console.log(error||results);
            return res.status(400);
        }
        let pass = results[0].retailerHashedPassword ;
        if (bcrypt.compareSync(retailerPassword, pass)){
            console.log(`${email} has successfully logged in\n`);
            return res.status(200).send('Retailer login successful!');
        }
        console.log(`${retailerEmail} COULD NOT login\n`);
        return res.status(400).send('Retailer login failed.');
    })
});


/**
 * Description: Get reatiler details
 * Request:     GET /retailerDetails
 * Send:
 * Receive:     JSON object of retailer details if successful, 400 otherwise
 */
app.get('/retailerDetails', (req, res) => {
    connection.query('Select * from RETAILER', (error, results) => {
        if(error) {
            console.log(error);
            return res.status(400).send('ERROR');
        }
        console.log(`Retailer details are:\n ${results} \n`);
        return res.status(400).send(JSON.parse(JSON.stringify(results)));
    })
});
function formatProductObject(product)
{
    return {
        itemCode: product.itemCode,
            model: product.model,
        name: product.brand,
        brand: product.brand,
        description: product.description,
        manufacturerName: product.manufacturerName,
        manufacturerLocation: product.manufacturerLocation,
        manufacturerTimestamp: product.manufacturerTimestamp,
        status:product.status.toString(),
        logisticProvider: product.deliveryInfo?.logistic,
        pickedOn: product.deliveryInfo?.pickedOn,
        deliveredOn: product.deliveryInfo?.deliveredOn,
        retailer: product.retailer
    }
}
app.post('/items',  async(req, res) => {
    console.log('Request to /items\n');
    let products = await contractInstance.methods.listCodes().call();
    const productResults = [];
    for(let product of products)
    {
        productResults.push(formatProductObject(product))
    }
    return res.status(200).send(JSON.parse(JSON.stringify(productResults)));
});


/**
 * Description: Sell product to a retailer
 * Request:     POST /sellProduct
 * Send:        JSON object which contains code, email
 * Receive:     200 if successful, 400 otherwise
 */
app.post('/sellProduct', async (req, res) => {

    let itemCode = req.body.itemCode;
    let retailerEmail = req.body.retailerEmail;
    if(!itemCode || !retailerEmail) return res.status(400).send({message:"Retail email and item code is required"})
    console.log('Request to /sellProduct\n', itemCode, retailerEmail);
    let hashedEmail = hashMD5(retailerEmail);
    const accounts = await web3.eth.getAccounts();
    console.log(`retailerEmail: ${retailerEmail}, hashed email: ${hashedEmail} \n`);
    try{
        let ok = await contractInstance.methods.sellProduct(itemCode, hashedEmail).send({
            from: accounts[0], gas: 3000000
        });
        if(!ok) {
            return res.status(400).send('Error');
        }
        console.log(`Successfully added ${hashedEmail} to code ${itemCode} \n`);
        return res.status(200).send('Success');
    }catch (e) {
        return res.status(400).send({message:e.innerError.message})
    }
});

/**
 * Description: Sell product to a retailer
 * Request:     POST /sellProduct
 * Send:        JSON object which contains code, email
 * Receive:     200 if successful, 400 otherwise
 */
app.post('/pickItemForDelivery', async (req, res) => {

    let itemCode = req.body.itemCode;
    let logisticEmail = req.body.logisticEmail;
    if(!itemCode || !logisticEmail) return res.status(400).send({message:"Logistic email and item code is required"})
    console.log('Request to /pickItemForDelivery\n', itemCode, logisticEmail);
    let hashedEmail = hashMD5(logisticEmail);
    const accounts = await web3.eth.getAccounts();
    console.log(`logisticEmail: ${logisticEmail}, hashed email: ${hashedEmail} \n`);
    try{
        const pickedTimestamp = new Date().toISOString();
        let ok = await contractInstance.methods
            .pickProductForDelivery(itemCode, hashedEmail,pickedTimestamp).send({
            from: accounts[0], gas: 3000000
        });
        if(!ok) {
            return res.status(400).send('Error');
        }
        console.log(`Successfully Picked  ${itemCode} by ${hashedEmail} for delivery\n`);
        return res.status(200).send('Success');
    }catch (e) {
        console.log(e)
        return res.status(400).send({message:e.innerError.message})
    }
});

/**
 * Description: Sell product to a retailer
 * Request:     POST /sellProduct
 * Send:        JSON object which contains code, email
 * Receive:     200 if successful, 400 otherwise
 */
app.post('/myDeliveries', async (req, res) => {
    let logisticEmail = req.body.logisticEmail;
    if(!logisticEmail) return res.status(400).send({message:"Logistic email is required"})
    console.log('Request to /myDeliveries\n', logisticEmail);
    let hashedEmail = hashMD5(logisticEmail);
    const accounts = await web3.eth.getAccounts();
    console.log(`logisticEmail: ${logisticEmail}, hashed email: ${hashedEmail} \n`);
    try{
        let codes = await contractInstance.methods.getDeliveriesCode(hashedEmail).call();
        const productResults = [];
        for(let itemCode of codes)
        {
            const product = await contractInstance.methods.getProductDetails(itemCode).call()
            productResults.push(formatProductObject(product))
        }
        return res.status(200).send(productResults);
    }catch (e) {
        return res.status(400).send({message:e.innerError.message})
    }
});

app.post('/markItemDelivered', async (req, res) => {
    let logisticEmail = req.body.logisticEmail;
    let itemCode = req.body.itemCode;
    if(!logisticEmail || !itemCode) return res.status(400).send({message:"Logistic email and item code is required"})
    console.log('Request to /markItemDelivered\n', logisticEmail);
    let hashedEmail = hashMD5(logisticEmail);
    const accounts = await web3.eth.getAccounts();
    console.log(`logisticEmail: ${logisticEmail}, hashed email: ${hashedEmail} \n`);
    try{
        await contractInstance.methods.markProductAsDelivered(itemCode,hashedEmail).send({
            from:accounts[0]
        });
        const product =await  contractInstance.methods.getProductDetails(itemCode).call()
        return res.status(200).send(formatProductObject(product));
    }catch (e) {
        return res.status(400).send({message:e.innerError?.message||e.message})
    }
});



/**
 * Description: Lists all the assets owned by the user
 * Request:     POST /myAssets
 * Send:        JSON object which contains email
 * Receive:     JSON array of objects which contain brand, model, description, status, manufacturerName,manufacturerLocation,
 *                                                  manufacturerTimestamp, retailerName, retailerLocation, retailerTimestamp
 */
app.post('/myProducts', async (req, res) => {
    console.log('Request to /myProducts\n');
    let myAssetsArray = [];
    let email = req.body.email;
    let hashedEmail = hashMD5(email);
    try{
        let productCodes = await contractInstance.methods.getRetailerProductCodes(hashedEmail).call();
        console.log(`Email ${email}`);
        const productResults = [];
        for (let code of productCodes) {
            const product = await contractInstance.methods.getProductDetails(code).call()
            productResults.push(formatProductObject(product));
        }
        res.status(200).send(productResults);
    }catch (e) {
        console.log(e)
        res.status(200).send([]);
    }

});


/**
 * Description: Lists all the assets owned by the user
 * Request:     POST /myAssets
 * Send:        JSON object which contains email
 * Receive:     JSON array of objects which contain brand, model, description, status, manufacturerName,manufacturerLocation,
 *                                                  manufacturerTimestamp, retailerName, retailerLocation, retailerTimestamp
 */
app.post('/confirmDelivery', async (req, res) => {
    console.log('Request to /confirmDelivery\n');
    let myAssetsArray = [];
    let email = req.body.email;
    let itemCode = req.body.itemCode;
    if(!email || !itemCode)
    {
        return res.status(400).send({message:"Item code and email is required"})
    }
    let hashedEmail = hashMD5(email);
    try{
        const accounts  = await web3.eth.getAccounts();
        const timestamp = new Date().toISOString();
        let products =
            await contractInstance.methods.confirmProductDelivery(itemCode, hashedEmail,timestamp).send({
            from:accounts[0],
                gas: 3000000
        });
        res.status(200).send("Success");
    }catch (e) {
        console.log(e)
        return res.status(500).send({message: e.innerError?.message||e.message})
    }
});



/**
 * Description: Get product details
 * Request:     POST /getProductDetails
 * Send:        JSON object which contains code
 * Receive:     JSON object whcih contains brand, model, description, status, manufacturerName, manufacturerLocation,
 *                                         manufacturerTimestamp, retailerName, retailerLocation, retailerTimestamp
 */
app.post('/getProductDetails', async (req, res) => {
    console.log('Request to /getProductDetails\n');
    let itemCode = req.body.itemCode;
    let email = req.body.email;
    try{
        const product = await contractInstance.methods.getProductDetails(itemCode).call();
        const formattedProduct = formatProductObject(product);
        if(formattedProduct.logisticProvider)
        {
            const logisticInfo = await contractInstance.methods.getLogisticDetails(formattedProduct.logisticProvider).call();
            formattedProduct.logisticName = logisticInfo[0];
            formattedProduct.logisticLocation = logisticInfo[1];
        }
        if(formattedProduct.retailer)
        {
            if(email){
                const hashedEmail = hashMD5(email);
                if(formattedProduct.retailer !== hashedEmail)
                {
                    formattedProduct.status = 5;
                }
            }

            const retailerInfo = await contractInstance.methods.getRetailerDetails(formattedProduct.retailer).call();
            formattedProduct.retailerName = retailerInfo[0];
            formattedProduct.retailerLocation = retailerInfo[1];
        }
        res.status(200).send(formattedProduct)
    }catch (e) {
        res.status(400).send("Not found")
    }

});





// app.post('/profile', (req, res) => {
//     console.log('Request to /profile\n');
//     let email = req.body.email;
//     await contractInstance.methods.
// });

// Function that creates an initial owner for a product
function initialOwner(code, retailerHashedEmail, customerHashedEmail) {
    return contractInstance.methods.initialOwner(code, retailerHashedEmail, customerHashedEmail,
                                        { from: web3.eth.accounts[0], gas: 3000000 });
}

// Function that creates transfers ownership of a product
function changeOwner(code, oldOwnerHashedEmail, newOwnerHashedEmail) {
    return contractInstance.methods.changeOwner(code, oldOwnerHashedEmail, newOwnerHashedEmail,
                                        { from: web3.eth.accounts[0], gas: 3000000 });
}


/**
 * Description: Gives product details if the scannee is not the owner of the product
 * Request:     POST /scan
 * Send:        JSON object which contains code
 * Receive:     JSON object which has productDetails
 */
app.post('/scan', async (req, res) => {
    console.log('Request made to /scan\n');
    let code = req.body.code;
    let productDetails = await  contractInstance.methods.getNotOwnedCodeDetails(code).call();
    let productDetailsObj = {
        'name': productDetails[0], 'model': productDetails[1], 'status': productDetails[2],
        'description': productDetails[3], 'manufacturerName': productDetails[4],
        'manufacturerLocation': productDetails[5], 'manufacturerTimestamp': productDetails[6]
    };
    console.log(`Code ${code} \n`, productDetails);
    res.status(200).send(JSON.stringify(productDetailsObj));
});


/**
 * Description: Generates QR codes for the manufacturers
 * Request:     POST /QRCodeForManufacturer
 * Send:        JSON object which contains brand, model, status, description, manufacturerName, manufacturerLocation
 * Receive:     200 if QR code was generated, 400 otherwise.
 */
app.post('/QRCodeForManufacturer', async (req, res) => {
    console.log('Request to /QRCodeForManufacturer\n');
    let brand = req.body.brand;
    let model = req.body.model;
    let description = req.body.description;
    let manufacturerName = req.body.manufacturerName;
    let manufacturerLocation = req.body.manufacturerLocation;
    if(!req.body.retailerEmail)
    {
        return res.status(400).send({message:"Retailer Email is required!"})
    }
    const retailerEmail = req.body.retailerEmail;
    let manufacturerTimestamp = new Date();         // Date() gives current timestamp
    manufacturerTimestamp = manufacturerTimestamp.toISOString().slice(0, 10);
    let salt = crypto.randomBytes(20).toString('hex');
    let code = hashMD5(brand + model  + description + manufacturerName + manufacturerLocation + salt);
    const accounts = await web3.eth.getAccounts()

    let ok = await contractInstance.methods.createProduct(code, brand, model, description, manufacturerName, manufacturerLocation,
                                        manufacturerTimestamp).send( { from: accounts[0], gas: 3000000 });
    console.log(`Brand: ${brand} \n`);
    if (!ok) {
        return res.status(400).send('ERROR! QR Code for manufacturer could not be generated.');
    }
    const hashedEmail =  hashMD5(retailerEmail);
    await contractInstance.methods.sellProduct(code,
        hashedEmail).send( { from: accounts[0], gas: 3000000 });

        console.log(`The QR Code generated is: ${code} \n`);
    let QRcode = code + '\n' + brand + '\n' + model + '\n' + description + '\n' + manufacturerName + '\n' + manufacturerLocation;
    fs.writeFile('views/davidshimjs-qrcodejs-04f46c6/code.txt', QRcode, (err, QRcode) => {
        if (err) {
            console.log(err);
        }
        console.log('Successfully written QR code to file!\n');
    });
    res.sendFile('views/davidshimjs-qrcodejs-04f46c6/index.html', { root: __dirname });
});


/**
 * Description: Gives all the customer details
 * Request:     GET /getCustomerDetails
 * Send:        JSON object which contains email
 * Receive:     JSON object which contains name, phone
 */
app.get('/getCustomerDetails', (req, res) => {
    console.log('Request to /getCustomerDetails\n');
    let email = req.body.email;
    let hashedEmail = hashMD5(email);
    let customerDetails = contractInstance.methods.getCustomerDetails(hashedEmail);
    console.log(`Email: ${email} \n`);
    let customerDetailsObj = {
        'name': customerDetails[0], 'phone': customerDetails[1]
    };
    res.status(200).send(JSON.parse(JSON.stringify(customerDetailsObj)));
});

// Server start
app.listen(port, (req, res) => {
    console.log(`Listening to port ${port}...\n`);
});
