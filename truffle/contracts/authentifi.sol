// SPDX-License-Identifier: MIT
pragma solidity ^0.8.13;

contract Authentifi {
    address public owner;

    // Arrays to store keys
    string[] private productCodes;

    // Enum to represent product states
    enum Status {
        Manufactured, // 0
        Sold,    // 1
        InTransit,    // 2
        MarkedAsDelivered,    // 3
        Delivered     // 4
    }

    struct DeliveryInfo {
        string logistic;
        string pickedOn;
        string deliveredOn;
    }

    struct Product {
        Status status;
        string brand;
        string itemCode;
        string model;
        string description;
        string manufacturerName;
        string manufacturerLocation;
        string manufacturerTimestamp;
        string retailer;
        DeliveryInfo deliveryInfo;
        string[] customers;
    }

    struct Retailer {
        string email;
        string name;
        string phone;
        string location;
        string[] productCodes;
        bool isValue;
    }

    struct Logistic {
        string name;
        string location;
        bool isValue;
        string[] productCodes;
    }

    mapping (string => Product) public productArray;
    mapping (string => Logistic) private logisticArray;
    mapping (string => Retailer) private retailerArray;

    // Define events
    event ProductCreated(string code, string brand, string model, Status status, string description, string manufacturerName, string manufacturerLocation, string manufacturerTimestamp);
    event ProductSold(string code, string retailer);
    event ProductPickedForDelivery(string code, string retailer);
    event LogisticCreated(string hashedEmail, string name, string phone);
    event RetailerCreated(string hashedEmail, string retailerName, string retailerLocation);
    event ProductDelivered(string code, string logistic);
    event ProductDeliveryConfirmed(string code, string retailer);


    constructor() {
        owner = msg.sender;
        assert(owner != address(0)); // Ensure owner is correctly set
    }

    modifier onlyOwner() {
        require(msg.sender == owner, "Not authorized");
        _;
    }

    function listCodes() public view returns (Product[] memory) {
        Product[] memory allProducts = new Product[](productCodes.length);
        for (uint i = 0; i < productCodes.length; i++) {
            allProducts[i] = productArray[productCodes[i]];
        }
        return allProducts;
    }

    // Function to create a new code for the product
    function createProduct(
        string memory _code,
        string memory _brand,
        string memory _model,
        string memory _description,
        string memory _manufacturerName,
        string memory _manufacturerLocation,
        string memory _manufacturerTimestamp
    ) public onlyOwner returns (uint) {
        Product memory newProduct;
        newProduct.brand = _brand;
        newProduct.model = _model;
        newProduct.status = Status.Manufactured;
        newProduct.itemCode = _code;
        newProduct.description = _description;
        newProduct.manufacturerName = _manufacturerName;
        newProduct.manufacturerLocation = _manufacturerLocation;
        newProduct.manufacturerTimestamp = _manufacturerTimestamp;
        productArray[_code] = newProduct;
        productCodes.push(_code);
        emit ProductCreated(_code, _brand, _model, Status.Manufactured, _description, _manufacturerName, _manufacturerLocation, _manufacturerTimestamp);
        return 1;
    }

    // Function for showing product details if the person scanning the product is not the owner
    function getProductDetails(string memory _code) public view returns (
        Product memory
    ) {
        Product memory product = productArray[_code];
        require(bytes(product.brand).length != 0,"Product does not exist"); // Ensure code exists
        return product;
    }

    // Function for showing product details if the person scanning the product is the owner
    function getProductRetailerDetails(string memory _code) public view returns (string memory, string memory) {
        Product memory product = productArray[_code];
        require(bytes(product.retailer).length != 0,"Product does not have retailer"); // Ensure code has a retailer
        Retailer memory retailer = retailerArray[product.retailer];
        return (retailer.name, retailer.location);
    }

    // Function for creating sell product
    function sellProduct(string memory _code, string memory _hashedEmailRetailer) public onlyOwner returns (uint) {
        Product storage product = productArray[_code];
        Retailer storage retailer = retailerArray[_hashedEmailRetailer];
        assert(bytes(product.brand).length != 0); // Ensure code exists
        require(product.status == Status.Manufactured, "Can not sell product");
        require(retailer.isValue == true, "Retailer does not exist");
        product.retailer = _hashedEmailRetailer;
        product.status = Status.Sold;
        emit ProductSold(_code, _hashedEmailRetailer);
        return 1;
    }

    // Function for creating a new customer
    function createLogistic(
        string memory _hashedEmail,
        string memory _name,
        string memory _location
    ) public onlyOwner returns (bool) {
        require(!logisticArray[_hashedEmail].isValue, "Retailer already exists");
        Logistic memory logistic;
        logistic.name = _name;
        logistic.location = _location;
        logistic.isValue = true;
        logisticArray[_hashedEmail] = logistic;
        emit LogisticCreated(_hashedEmail, _name, _location);
        return true;
    }

    function getLogisticDetails(string memory _hashedEmail) public view returns (string memory, string memory) {
        Logistic memory logistic = logisticArray[_hashedEmail];
        require(logistic.isValue, "Logistic provider does not exists"); // Ensure customer exists
        return (logistic.name, logistic.location);
    }

    function createRetailer(
        string memory _hashedEmail,
        string memory _retailerName,
        string memory _retailerLocation
    ) public onlyOwner returns (uint) {
        if (retailerArray[_hashedEmail].isValue) {
            return 0;
        }
        Retailer memory newRetailer;
        newRetailer.name = _retailerName;
        newRetailer.location = _retailerLocation;
        newRetailer.isValue = true;
        retailerArray[_hashedEmail] = newRetailer;
        emit RetailerCreated(_hashedEmail, _retailerName, _retailerLocation);
        return 1;
    }

    function getRetailerProductCodes(string memory _retailer) public view returns (string[] memory)  {
        Retailer storage retailer = retailerArray[_retailer];
        require(retailer.isValue==true,"Retailer does not exists"); // Ensure code exists
        return retailer.productCodes;
    }

    function confirmProductDelivery(string memory _code, string memory _retailer, string memory _timestamp) public payable returns(bool) {
        Product storage product = productArray[_code];
        Retailer storage retailer = retailerArray[_retailer];
        require(bytes(product.brand).length != 0, "_retailer does not exist"); // Ensure code exists
        require(product.status == Status.MarkedAsDelivered,"Product needs to be in transit"); // Ensure code exists
        require(retailer.isValue == true,"Logistic provider does not exist"); // Ensure logistic exists
        require(bytes(product.deliveryInfo.logistic).length != 0,"Delivery information is missing"); // Ensure logistic exists
        product.status = Status.Delivered;
        product.deliveryInfo.deliveredOn = _timestamp;
        retailer.productCodes.push(_code);
        emit ProductDeliveryConfirmed(_code, _retailer);
        return true;
    }

    function getRetailerDetails(string memory _hashedEmail) public view returns (string memory, string memory) {
        Retailer memory retailer = retailerArray[_hashedEmail];
        require(retailer.isValue,"Retailer does not exsits"); // Ensure retailer exists
        return (retailer.name, retailer.location);
    }


    function pickProductForDelivery(string memory _code, string memory _logistic,string memory _pickedTimestamp) public payable returns(bool) {
        Product storage product = productArray[_code];
        Logistic storage logistic = logisticArray[_logistic];
        require(bytes(product.brand).length != 0); // Ensure code exists
        require(product.status == Status.Sold,"Product has can not be pick up for delivery"); // Ensure code exists
        require(logistic.isValue == true,"Logistic provider does not exist"); // Ensure logistic exists
            logistic.productCodes.push(_code); // Adding customer in code
            product.deliveryInfo.logistic = _logistic;
            product.deliveryInfo.pickedOn = _pickedTimestamp;
            product.status = Status.InTransit;
            emit ProductPickedForDelivery(_code, _logistic);
            return true;
    }

    function getDeliveriesCode(string memory _logistic) public view returns (string[] memory)  {
        Logistic storage logistic = logisticArray[_logistic];
        require(logistic.isValue==true,"Logistic Provider does not exists"); // Ensure code exists
        return logistic.productCodes;
    }

    function markProductAsDelivered(string memory _code, string memory _logistic) public payable returns(bool) {
        Product storage product = productArray[_code];
        Logistic storage logistic = logisticArray[_logistic];
        require(bytes(product.brand).length != 0, "Product does not exist"); // Ensure code exists
        require(product.status == Status.InTransit,"Product needs to be in transit"); // Ensure code exists
        require(logistic.isValue == true,"Logistic provider does not exist"); // Ensure logistic exists
        //require(compareStrings(product.deliveryInfo.logistic, _logistic),"Logistic Provider can not deliver product"); // Ensure logistic exists
        product.status = Status.MarkedAsDelivered;
        emit ProductDelivered(_code, _logistic);
        return true;
    }



    // Cannot directly compare strings in Solidity
    // This function hashes the 2 strings and then compares the 2 hashes
    function compareStrings(string memory a, string memory b) internal pure returns (bool) {
        return keccak256(abi.encodePacked(a)) == keccak256(abi.encodePacked(b));
    }

}
