package com.tugalsan.api.network.server;

import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class TS_NetworkSmbUtils {

    public static TGS_UnionExcuse<SmbFile> of(CharSequence smbLoc, CharSequence username, CharSequence password) {
        return TGS_UnSafe.call(() -> {
            var ctx = SingletonContext.getInstance();
            var auth = new NtlmPasswordAuthentication(ctx, null, username == null ? null : username.toString(), password == null ? null : password.toString());
            var ctxWithCredentials = SingletonContext.getInstance().withCredentials(auth);
            return TGS_UnionExcuse.of(new SmbFile(smbLoc.toString(), ctxWithCredentials));
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<byte[]> readBytes(SmbFile smbFile) {
        return readBytes_fromUrlConnection(smbFile);
    }

    private static TGS_UnionExcuse<byte[]> readBytes_fromUrlConnection(URLConnection urlConnection) {
        return TGS_UnSafe.call(() -> {
            return TGS_UnionExcuse.of(urlConnection.getInputStream().readAllBytes());
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<List<SmbFile>> list(SmbFile smbPath) {
        return TGS_UnSafe.call(() -> {
            return TGS_UnionExcuse.of(TGS_StreamUtils.toLst(Arrays.stream(smbPath.listFiles())));
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }
}
