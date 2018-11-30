package com.hunter.wallet.utils;


import android.util.Log;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Web3jUtils {
    private static final String URL = "https://mainnet.infura.io/c0oGHqQQlq6XJU2kz5DL";
    private static Web3j web3j;

    private static Web3j getWeb3j() {
        if (null == web3j) {
            web3j = Web3j.build(new HttpService(URL));
        }
        return web3j;
    }

    private static List<Type> erc20FunCall(Web3j web3j, byte[] contract, String fun, List<Type> inputParameters,
                                           List<TypeReference<?>> outputParameters) throws ExecutionException, InterruptedException {
        return erc20FunCall(web3j, contract, Numeric.hexStringToByteArray("0x0000000000000000000000000000000000000000"), fun, inputParameters,
                outputParameters);
    }

    private static List<Type> erc20FunCall(Web3j web3j, byte[] contract, byte[] form, String fun, List<Type> inputParameters,
                                           List<TypeReference<?>> outputParameters) throws ExecutionException, InterruptedException {
        Function function = new Function(fun, inputParameters, outputParameters);
        Transaction transaction = Transaction.createEthCallTransaction(Numeric.toHexString(form), Numeric.toHexString(contract),
                FunctionEncoder.encode(function));
        EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
        if (!ethCall.hasError()) {
            return FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
        } else {
            return null;
        }
    }

    private static Utf8String erc20Name(byte[] contract) throws ExecutionException, InterruptedException {
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        outputParameters.add(new TypeReferenceExtend<Utf8String>() {
        });
        List<Type> ret = erc20FunCall(getWeb3j(), contract, "name", inputParameters, outputParameters);
        if (ret != null && ret.size() > 0) {
            return (Utf8String) ret.get(0);
        } else {
            return null;
        }
    }

    private static Utf8String erc20Symbol(byte[] contract) throws ExecutionException, InterruptedException {
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        outputParameters.add(new TypeReferenceExtend<Utf8String>() {
        });
        List<Type> ret = erc20FunCall(getWeb3j(), contract, "symbol", inputParameters, outputParameters);
        if (ret != null && ret.size() > 0) {
            return (Utf8String) ret.get(0);
        } else {
            return null;
        }
    }

    private static Uint8 erc20Decimals(byte[] contract) throws ExecutionException, InterruptedException {
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        outputParameters.add(new TypeReferenceExtend<Uint8>() {
        });
        List<Type> ret = erc20FunCall(getWeb3j(), contract, "decimals", inputParameters, outputParameters);
        if (ret != null && ret.size() > 0) {
            return (Uint8) ret.get(0);
        } else {
            return null;
        }
    }

    private static Uint256 erc20TotalSupply(byte[] contract) throws ExecutionException, InterruptedException {
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        outputParameters.add(new TypeReferenceExtend<Uint256>() {
        });
        List<Type> ret = erc20FunCall(getWeb3j(), contract, "totalSupply", inputParameters, outputParameters);
        if (ret != null && ret.size() > 0) {
            return (Uint256) ret.get(0);
        } else {
            return null;
        }
    }

    private static Uint256 erc20BalanceOf(byte[] contract, byte[] address) throws ExecutionException, InterruptedException {
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Address(new BigInteger(1, address)));

        List<TypeReference<?>> outputParameters = new ArrayList<>();
        outputParameters.add(new TypeReferenceExtend<Uint256>() {
        });
        List<Type> ret = erc20FunCall(getWeb3j(), contract, "balanceOf", inputParameters, outputParameters);
        if (ret != null && ret.size() > 0) {
            return (Uint256) ret.get(0);
        } else {
            return null;
        }
    }

    private static Uint256 erc20Allowance(byte[] contract, byte[] owner, byte[] spender) throws ExecutionException, InterruptedException {
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Address(new BigInteger(owner)));
        inputParameters.add(new Address(new BigInteger(spender)));

        List<TypeReference<?>> outputParameters = new ArrayList<>();
        outputParameters.add(new TypeReferenceExtend<Uint256>() {
        });
        List<Type> ret = erc20FunCall(getWeb3j(), contract, "allowance", inputParameters, outputParameters);
        if (ret != null && ret.size() > 0) {
            return (Uint256) ret.get(0);
        } else {
            return null;
        }
    }

    public static String getTokenSymbol(byte[] contract) {
        try {
            Utf8String symbol = erc20Symbol(contract);
            return symbol != null ? symbol.getValue() : null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getTokenName(byte[] contract) {
        try {
            Utf8String name = erc20Name(contract);
            return name != null ? name.getValue() : null;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer getTokenDecimals(byte[] contract) {
        try {
            Uint8 decimals = erc20Decimals(contract);
            if (decimals != null) {
                return Integer.valueOf(decimals.getValue().toString());
            } else {
                return null;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BigInteger getTotalSupply(byte[] contract) {
        try {
            Uint256 totalSupply = erc20TotalSupply(contract);
            return totalSupply != null ? totalSupply.getValue() : null;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BigInteger getTokenBalance(byte[] contract, byte[] addr) {
        try {
            Uint256 balance = erc20BalanceOf(contract, addr);
            return balance != null ? balance.getValue() : null;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BigDecimal getEthBalance(byte[] address) {
        try {
            EthGetBalance ethGetBalance = getWeb3j().ethGetBalance(Numeric.toHexString(address), DefaultBlockParameterName.LATEST).send();
            if (ethGetBalance.hasError()) {
                Log.i("getEthBalance", "error : " + ethGetBalance.getError().getMessage());
                return null;
            } else {
                return Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
