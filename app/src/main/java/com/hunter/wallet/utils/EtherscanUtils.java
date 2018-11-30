package com.hunter.wallet.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;

public class EtherscanUtils {

    private static final String apiUrl = "http://api.etherscan.io/api";
    private static final String apikey = "c0oGHqQQlq6XJU2kz5DL";
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static class ApiResult<T> {
        private String status;
        private String message;
        private T result;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getResult() {
            return result;
        }

        public void setResult(T result) {
            this.result = result;
        }
    }

    public static class ProxyResult<T> {
        private String jsonrpc;
        private int id;
        private T result;

        public String getJsonrpc() {
            return jsonrpc;
        }

        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public T getResult() {
            return result;
        }

        public void setResult(T result) {
            this.result = result;
        }
    }

    public static BigInteger getRecentBlock() throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("apikey", apikey)
                .add("module", "proxy")
                .add("action", "eth_blockNumber")
                .build();

        ProxyResult<String> result = objectMapper.readValue(HTTPUtils.doPostSync(apiUrl, formBody), new TypeReference<ProxyResult<String>>() {
        });
        return Numeric.toBigInt(result.getResult());
    }

    public static BigDecimal getEthBalance(byte[] addr) throws IOException {

        FormBody formBody = new FormBody.Builder()
                .add("apikey", apikey)
                .add("module", "account")
                .add("action", "balance")
                .add("tag", "latest")
                .add("address", Numeric.toHexString(addr))
                .build();

        ApiResult<String> result = objectMapper.readValue(HTTPUtils.doPostSync(apiUrl, formBody), new TypeReference<ApiResult<String>>() {
        });
        if (result != null && result.getStatus().equals("1")) {
            return Convert.fromWei(result.getResult(), Convert.Unit.ETHER);
        }
        return new BigDecimal("0");
    }

    public static BigDecimal getTokenBalance(byte[] addr, byte[] contract, int decimals)
            throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("apikey", apikey)
                .add("module", "account")
                .add("action", "tokenbalance")
                .add("tag", "latest")
                .add("contractaddress", Numeric.toHexString(contract))
                .add("address", Numeric.toHexString(addr))
                .build();
        ApiResult<String> result = objectMapper.readValue(HTTPUtils.doPostSync(apiUrl, formBody), new TypeReference<ApiResult<String>>() {
        });
        if (result != null && result.getStatus().equals("1")) {
            return new BigDecimal(result.getResult()).divide(BigDecimal.TEN.pow(decimals));
        }
        return new BigDecimal("0");
    }


    public static List<Transaction> getTransactions(byte[] addr, BigInteger startBlock, BigInteger endBlock)
            throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("apikey", apikey)
                .add("module", "account")
                .add("action", "txlist")
                .add("address", Numeric.toHexString(addr))
                .add("startblock", startBlock != null ? startBlock.toString() : "")
                .add("endblock", endBlock != null ? endBlock.toString() : "")
                .add("sort", "asc")
                .build();

        ApiResult<List<Transaction>> result = objectMapper.readValue(HTTPUtils.doPostSync(apiUrl, formBody), new TypeReference<ApiResult<List<Transaction>>>() {
        });
        if (result != null && result.getStatus().equals("1")) {
            return result.getResult();
        } else {
            return new ArrayList<>();
        }
    }

    public static List<TokenTx> getTokenTx(byte[] addr, byte[] contractaddress, BigInteger startBlock, BigInteger endBlock)
            throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("apikey", apikey)
                .add("module", "account")
                .add("action", "tokentx")
                .add("contractaddress", Numeric.toHexString(contractaddress))
                .add("address", Numeric.toHexString(addr))
                .add("startblock", startBlock != null ? startBlock.toString() : "")
                .add("endblock", endBlock != null ? endBlock.toString() : "")
                .add("sort", "asc")
                .build();

        ApiResult<List<TokenTx>> result = objectMapper.readValue(HTTPUtils.doPostSync(apiUrl, formBody), new TypeReference<ApiResult<List<TokenTx>>>() {
        });
        if (result != null && result.getStatus().equals("1")) {
            return result.getResult();
        } else {
            return new ArrayList<>();
        }
    }

    public static BigInteger getGasPrice() throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("apikey", apikey)
                .add("module", "proxy")
                .add("action", "eth_gasPrice")
                .build();
        ProxyResult<String> result = objectMapper.readValue(HTTPUtils.doPostSync(apiUrl, formBody), new TypeReference<ProxyResult<String>>() {
        });
        return Numeric.toBigInt(result.getResult());
    }

    public static BigDecimal getEthPriceUsd() throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("apikey", apikey)
                .add("module", "stats")
                .add("action", "ethprice")
                .build();
        ApiResult<EthPrice> result = objectMapper.readValue(HTTPUtils.doPostSync(apiUrl, formBody), new TypeReference<ApiResult<EthPrice>>() {
        });
        return new BigDecimal(result.getResult().getEthusd());
    }

    private static class EthPrice {
        private String ethbtc;
        private String ethbtc_timestamp;
        private String ethusd;
        private String ethusd_timestamp;

        public String getEthbtc() {
            return ethbtc;
        }

        public void setEthbtc(String ethbtc) {
            this.ethbtc = ethbtc;
        }

        public String getEthbtc_timestamp() {
            return ethbtc_timestamp;
        }

        public void setEthbtc_timestamp(String ethbtc_timestamp) {
            this.ethbtc_timestamp = ethbtc_timestamp;
        }

        public String getEthusd() {
            return ethusd;
        }

        public void setEthusd(String ethusd) {
            this.ethusd = ethusd;
        }

        public String getEthusd_timestamp() {
            return ethusd_timestamp;
        }

        public void setEthusd_timestamp(String ethusd_timestamp) {
            this.ethusd_timestamp = ethusd_timestamp;
        }
    }

    public static class TokenTx {
        private String blockNumber;
        private String timeStamp;
        private String hash;
        private String nonce;
        private String blockHash;
        private String from;
        private String contractAddress;
        private String to;
        private String value;
        private String tokenName;
        private String tokenSymbol;
        private String tokenDecimal;
        private String transactionIndex;
        private String gas;
        private String gasPrice;
        private String gasUsed;
        private String cumulativeGasUsed;
        private String input;
        private String confirmations;

        public String getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getBlockHash() {
            return blockHash;
        }

        public void setBlockHash(String blockHash) {
            this.blockHash = blockHash;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getContractAddress() {
            return contractAddress;
        }

        public void setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getTokenName() {
            return tokenName;
        }

        public void setTokenName(String tokenName) {
            this.tokenName = tokenName;
        }

        public String getTokenSymbol() {
            return tokenSymbol;
        }

        public void setTokenSymbol(String tokenSymbol) {
            this.tokenSymbol = tokenSymbol;
        }

        public String getTokenDecimal() {
            return tokenDecimal;
        }

        public void setTokenDecimal(String tokenDecimal) {
            this.tokenDecimal = tokenDecimal;
        }

        public String getTransactionIndex() {
            return transactionIndex;
        }

        public void setTransactionIndex(String transactionIndex) {
            this.transactionIndex = transactionIndex;
        }

        public String getGas() {
            return gas;
        }

        public void setGas(String gas) {
            this.gas = gas;
        }

        public String getGasPrice() {
            return gasPrice;
        }

        public void setGasPrice(String gasPrice) {
            this.gasPrice = gasPrice;
        }

        public String getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
        }

        public String getCumulativeGasUsed() {
            return cumulativeGasUsed;
        }

        public void setCumulativeGasUsed(String cumulativeGasUsed) {
            this.cumulativeGasUsed = cumulativeGasUsed;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(String confirmations) {
            this.confirmations = confirmations;
        }
    }

    public static class Transaction {
        private String timeStamp;
        private String hash;
        private String from;
        private String to;
        private String value;
        private String gas;
        private String gasPrice;
        private String contractAddress;
        private String blockNumber;
        private String nonce;
        private String blockHash;
        private String transactionIndex;
        private String isError;
        private String txreceipt_status;
        private String input;
        private String cumulativeGasUsed;
        private String gasUsed;
        private String confirmations;

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getGas() {
            return gas;
        }

        public void setGas(String gas) {
            this.gas = gas;
        }

        public String getGasPrice() {
            return gasPrice;
        }

        public void setGasPrice(String gasPrice) {
            this.gasPrice = gasPrice;
        }

        public String getContractAddress() {
            return contractAddress;
        }

        public void setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
        }

        public String getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getBlockHash() {
            return blockHash;
        }

        public void setBlockHash(String blockHash) {
            this.blockHash = blockHash;
        }

        public String getTransactionIndex() {
            return transactionIndex;
        }

        public void setTransactionIndex(String transactionIndex) {
            this.transactionIndex = transactionIndex;
        }

        public String getIsError() {
            return isError;
        }

        public void setIsError(String isError) {
            this.isError = isError;
        }

        public String getTxreceipt_status() {
            return txreceipt_status;
        }

        public void setTxreceipt_status(String txreceipt_status) {
            this.txreceipt_status = txreceipt_status;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getCumulativeGasUsed() {
            return cumulativeGasUsed;
        }

        public void setCumulativeGasUsed(String cumulativeGasUsed) {
            this.cumulativeGasUsed = cumulativeGasUsed;
        }

        public String getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
        }

        public String getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(String confirmations) {
            this.confirmations = confirmations;
        }
    }
}
