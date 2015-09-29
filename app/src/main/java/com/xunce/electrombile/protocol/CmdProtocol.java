package com.xunce.electrombile.protocol;

/**
 * Created by lybvinci on 2015/9/28.
 */
public class CmdProtocol extends Protocol {
    protected String cmd;
    protected int result;
    protected String state;

    public CmdProtocol(String tmp) {
        super(tmp);
    }

    @Override
    public String getCmd() {
        cmd = keyForValue(JsonKeys.CMD);
        return cmd;
    }

    @Override
    public int getResult() {
        result = Integer.parseInt(keyForValue(JsonKeys.RESULT));
        return result;
    }

    @Override
    public String getState() {
        state = keyForValue(JsonKeys.STATE);
        return state;
    }

}