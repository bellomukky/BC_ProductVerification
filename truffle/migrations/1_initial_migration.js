var Authentifi = artifacts.require("Authentifi");

module.exports = function(deployer) {
    // deployment steps
    deployer.deploy(Authentifi);
};