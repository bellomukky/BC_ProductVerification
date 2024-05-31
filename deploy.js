const express = require('express');
const {Web3} = require('web3');
const fs = require('fs');
const {abiArray, contractByteCode} = require("./abiArray");

// Web3 connection
const web3 = new Web3(new Web3.providers.HttpProvider('http://127.0.0.1:7545'));
console.log(`Talking with a geth server \n`);

const myContract = new web3.eth.Contract(abiArray);
myContract.handleRevert = true;

async function deploy(){
    const providersAccounts = await web3.eth.getAccounts();
    const defaultAccount = providersAccounts[0];
    console.log('deployer account:', defaultAccount);

    const contractDeployer = myContract.deploy({
        data: contractByteCode,
        arguments: [],
    });

    const gas = await contractDeployer.estimateGas({
        from: defaultAccount,
    });
    console.log('estimated gas:', gas);

    try {
        const tx = await contractDeployer.send({
            from: defaultAccount,
            gas,
            gasPrice: 10000000000,
        });
        console.log('Contract deployed at address: ' + tx.options.address);

    } catch (error) {
        console.error("Goodsdsd",error);
        process.exit(1);
    }
}

deploy().then(()=>{
    process.exit(1)
}).catch(error=>{
    console.log(error)
});



// Try connecting using IPv4 localhost


