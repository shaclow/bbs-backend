package com.bbsbackend.components.tokenGenerator.repository;

import java.time.Instant;
import java.util.Hashtable;
import java.util.Map;

import com.google.gson.Gson;

public class TokenGeneratorMemoryRepository implements TokenGeneratorRepository{
	  private final static Gson gson = new Gson();
	    private final String repositoryName;

	    // Token -> Instant
	    private final Map<String, String> tokenToExpireTime = new Hashtable<>();

	    private TokenGeneratorMemoryRepository(String repositoryName) {
	        this.repositoryName = repositoryName;
	    }

	    @Override
	    public void saveToken(String token, Instant expireTime) {
	        if (!tokenToExpireTime.containsKey(token)) tokenToExpireTime.put(token, gson.toJson(expireTime));
	    }

	    @Override
	    public Instant getExpireTime(String token) {
	        if (token == null) return null;
	        else return gson.fromJson(tokenToExpireTime.get(token), Instant.class);
	    }

	    @Override
	    public void delete(String token) {
	        if (token != null) tokenToExpireTime.remove(token);
	    }

	    @Override
	    public String getRepositoryName() {
	        return this.repositoryName;
	    }

	    public static TokenGeneratorRepository get(String repositoryName) {
	        return new TokenGeneratorMemoryRepository(repositoryName);
	    }
}
