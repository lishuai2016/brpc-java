/*
 * Copyright (c) 2018 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.brpc.protocol;

import java.lang.reflect.Method;
import java.util.Map;

import com.baidu.brpc.RpcMethodInfo;
import com.baidu.brpc.client.RpcCallback;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.channel.BrpcChannelGroup;
import com.baidu.brpc.exceptions.NotEnoughDataException;

import io.netty.channel.ChannelFuture;

public abstract class AbstractProtocol implements Protocol {

    protected static NotEnoughDataException notEnoughDataException = new NotEnoughDataException();

    @Override
    public Request createRequest() {
        // tcp protocol implementation, http protocols should override this method
        return new RpcRequest();
    }

    @Override
    public Response createResponse() {
        // tcp protocol implementation, http protocols should override this method
        return new RpcResponse();
    }

    @Override
    public Response getResponse() {
        // tcp protocol implementation, http protocols should override this method
        Response response = RpcResponse.getRpcResponse();
        response.reset();
        return response;
    }

    @Override
    public Request initRequest(RpcClient rpcClient, Map<String, RpcMethodInfo> rpcMethodMap, Object instance,
                               Method method, Object[] args) {
        String methodName = method.getName();
        RpcMethodInfo rpcMethodInfo = rpcMethodMap.get(methodName);
        Request request = this.createRequest();
        request.setCompressType(rpcClient.getRpcClientOptions().getCompressType().getNumber());
        request.setTarget(instance);
        request.setRpcMethodInfo(rpcMethodInfo);
        request.setTargetMethod(rpcMethodInfo.getMethod());
        request.setServiceName(rpcMethodInfo.getServiceName());
        request.setMethodName(rpcMethodInfo.getMethodName());
        request.setNsHeadMeta(rpcMethodInfo.getNsHeadMeta());
        int argLength = args.length;
        if (argLength > 1 && args[argLength - 1] instanceof RpcCallback) {
            // 异步调用
            argLength = argLength - 1;
            Object[] newArgs = new Object[argLength];
            for (int i = 0; i < newArgs.length; i++) {
                newArgs[i] = args[i];
            }
            request.setArgs(newArgs);
        } else {
            // 同步调用
            request.setArgs(args);
        }
        // attachment
        RpcContext rpcContext = RpcContext.getContext();
        request.setKvAttachment(rpcContext.getRequestKvAttachment());
        request.setBinaryAttachment(rpcContext.getRequestBinaryAttachment());
        return request;
    }

    @Override
    public void beforeRequestSent(Request request, RpcClient rpcClient, BrpcChannelGroup channelGroup) {
        // By default, in tcp protocols, there's nothing to to
    }

    @Override
    public boolean returnChannelBeforeResponse() {
        return true;
    }

    @Override
    public void afterResponseSent(Request request, Response response, ChannelFuture channelFuture) {
        // By default, in tcp protocols, there's nothing to to
    }
}
