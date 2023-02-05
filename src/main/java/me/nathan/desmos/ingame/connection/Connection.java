package me.nathan.desmos.ingame.connection;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;

import me.nathan.desmos.ingame.account.Account;

import me.nathan.futureclient.framework.auth.phase.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

/**
 * Ported from desmos1.0
 */
public class Connection {

    public static long timeOfLastLogin;

    public static void login(Account account, String ip, int port) throws Exception {
        String code = Code.getInitialCode(account.getEmail(), account.getPassword(), String.valueOf(UUID.randomUUID()));

        MSToken.TokenPair msToken = MSToken.getForUserPass(code);
        XBLToken.XBLTokenType xblToken = XBLToken.getForUserPass(msToken.token);
        LSToken.LSTokenType lsToken = LSToken.getFor(xblToken.token);
        MCToken.MCTokenType minecraftToken = MCToken.getFor(lsToken);
        MCToken.Profile profile = MCToken.getProfile(minecraftToken);

        AuthenticationService authService = new AuthenticationService();
        authService.setUsername(profile.name);
        authService.setAccessToken(minecraftToken.accessToken);

        GameProfile gameProfile = new GameProfile(
                UUID.fromString(profile.uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                        "$1-$2-$3-$4-$5")),
                profile.name
        );
        account.setUsername(gameProfile.getName());

        Field loggedInField = AuthenticationService.class.getDeclaredField("loggedIn");
        loggedInField.setAccessible(true);
        loggedInField.setBoolean(authService, true);

        Field idField = AuthenticationService.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(authService, minecraftToken.username);

        Field profilesField = AuthenticationService.class.getDeclaredField("profiles");
        profilesField.setAccessible(true);
        List<GameProfile> profiles = (List<GameProfile>)profilesField.get(authService);
        profiles.add(gameProfile);

        Field selectedProfileField = AuthenticationService.class.getDeclaredField("selectedProfile");
        selectedProfileField.setAccessible(true);
        selectedProfileField.set(authService, gameProfile);

        MinecraftProtocol protocol = new MinecraftProtocol(authService.getSelectedProfile(), authService.getAccessToken());
        Client client = new Client(ip, port, protocol, new TcpSessionFactory(null));
        account.setSession(new TcpClientSession(ip, port, protocol, client, null));

        account.getSession().connect();

        if(account.getConnectionAdapter() == null) {
            ConnectionAdapter adapter = new ConnectionAdapter(account);
            account.getSession().addListener(adapter);
            account.setConnectionAdapter(adapter);
        } else {
            account.getSession().addListener(account.getConnectionAdapter());
        }
    }
}
