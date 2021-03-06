package com.bbsbackend.components.authenticator.repository;

import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import com.bbsbackend.components.authenticator.common.Permission;
import com.bbsbackend.components.authenticator.common.ResetToken;
import com.bbsbackend.components.authenticator.common.UserToken;
import com.bbsbackend.util.Tuple;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AuthenticatorMemoryRepository implements AuthenticatorRepository{
    private static final Gson gson = new Gson();
    private final String repositoryName;

    // Username -> Tuple<Password, Permission>
    private final Map<String, String> usernameToPwdPermission = new Hashtable<>();

    // UserToken -> Permission(UserId,role)
    private final Map<String, String> userTokenToPermission = new Hashtable<>();

    // ResetToken -> Username
    private final Map<String, String> resetTokenToUsername = new Hashtable<>();

    private AuthenticatorMemoryRepository(String repositoryName) {
        this.repositoryName = repositoryName;
    }
	@Override
	public boolean contains(String username) {
			return usernameToPwdPermission.containsKey(username);
	}

	@Override
	public boolean saveUser(String username, String password, Permission permission) {
		usernameToPwdPermission.put(username, gson.toJson(Tuple.of(password, permission)));
		return true;
	}

	@Override
	public boolean saveUserToken(UserToken token, Permission permission) {
		userTokenToPermission.put(token.value,gson.toJson(permission));
		return true;
	}

    @Override
    public boolean saveResetToken(ResetToken token, String username) {
        resetTokenToUsername.put(token.value, username);
        return true;
    }

	@Override
	public String getPassword(String username) {
		String t=usernameToPwdPermission.get(username);
		if(t==null) return null;
		return jsonToPwdPms(t).left;
	}

	@Override
	public boolean setPassword(String username, String password) {
		// TODO Auto-generated method stub
		String t=usernameToPwdPermission.get(username);
		if(t==null) return false;
		usernameToPwdPermission.put(username, gson.toJson(jsonToPwdPms(t).modLeft(password)));
		return true;
	}

	@Override
	public Permission getPermissionByUsername(String username) {
        String t = usernameToPwdPermission.get(username);
        if (t == null) return null;

        return jsonToPwdPms(t).right;
	}

	@Override
	public Permission getPermissionByToken(UserToken token) {
		return gson.fromJson(userTokenToPermission.get(token.value), Permission.class);
	}

	@Override
	public String getUsernameByResetToken(ResetToken token) {
		  return resetTokenToUsername.get(token.value);
	}

	@Override
	public void deleteUser(String username) {
		Optional.ofNullable(usernameToPwdPermission.get(username)).ifPresent(t->userTokenToPermission.entrySet()
				.removeIf(p->gson.fromJson(p.getValue(), Permission.class).equals(jsonToPwdPms(t).right)));
		resetTokenToUsername.entrySet().removeIf(p -> p.getValue().equals(username));
	    userTokenToPermission.remove(username);
	}

	@Override
	public void deleteUserToken(UserToken token) {
		   userTokenToPermission.remove(token.value);
		
	}

	@Override
	public void deleteUserTokenByUsername(String username) {
        String t = usernameToPwdPermission.get(username);
        if (t == null) return;

        String userId = jsonToPwdPms(t).right.userId;
        userTokenToPermission.entrySet().removeIf(e ->
                gson.fromJson(e.getValue(), Permission.class).userId.equals(userId)
        );
		
	}

	@Override
	public void deleteResetToken(ResetToken token) {
	    resetTokenToUsername.remove(token.value);
		
	}

	@Override
	public String getRepositoryName() {
		 return this.repositoryName;
	}
    public static AuthenticatorRepository get(String repositoryName) {
        return new AuthenticatorMemoryRepository(repositoryName);
    }

    private static Tuple<String, Permission> jsonToPwdPms(String json) {
        return gson.fromJson(json, new TypeToken<Tuple<String, Permission>>(){}.getType());
    }

}
