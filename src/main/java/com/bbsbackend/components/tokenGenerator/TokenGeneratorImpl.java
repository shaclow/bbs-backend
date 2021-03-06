package com.bbsbackend.components.tokenGenerator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Random;
import java.util.stream.IntStream;

import javax.xml.bind.DatatypeConverter;

import com.bbsbackend.components.tokenGenerator.repository.TokenGeneratorRepository;
/*
 * getToken不懂
 */
public class TokenGeneratorImpl implements TokenGenerator{
	private final String componentName;
	private final TokenGeneratorRepository repository;
	
	public TokenGeneratorImpl(String componentName,TokenGeneratorRepository repository) {
		this.componentName=componentName;
		this.repository=repository;
	}

	@Override
	public String genToken(String key, Instant expireTime) {
		try {
			MessageDigest md=MessageDigest.getInstance("MD5");
			md.update(key.getBytes());
			md.update(String.valueOf(expireTime.toEpochMilli()).getBytes());
			md.update(randomString(16).getBytes());
	        String hash = DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
	        repository.saveToken(hash, expireTime);
	        return hash;
		}catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isExpired(String token) {
		Instant expireTime=repository.getExpireTime(token);
		if(expireTime==null) return true;
		if(Instant.now().isAfter(expireTime)) {
			repository.delete(token);
			return true;
		}
		return false;
	}

	@Override
	public void expire(String token) {
		repository.delete(token);
	}

	@Override
	public String getName() {
		return this.componentName;
	}
    private static final String STRING =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdedfhijklmnopqrstuvwxyz" +
                    "0123456789" +
                    "/*-.,;:'\"\\|[]{}()<>?!@#$%^&`~=_";
    
    private static String randomString(int count) {
    	StringBuilder builder=new StringBuilder();
    	int length=STRING.length();
    	Random random=new Random();
    	IntStream.range(0, count).forEach(i->{
    		int index=random.nextInt(length);
    		builder.append(STRING.charAt(index));
    		
    	});
    	return builder.toString();
    }
	
}
