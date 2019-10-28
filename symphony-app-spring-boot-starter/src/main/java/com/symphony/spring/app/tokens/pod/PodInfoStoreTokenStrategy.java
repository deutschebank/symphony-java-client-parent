package com.symphony.spring.app.tokens.pod;

import java.util.Map;

import javax.net.ssl.TrustManager;

import com.symphony.api.ApiBuilder;
import com.symphony.api.ConfigurableApiBuilder;
import com.symphony.api.authenticator.AuthenticationApi;
import com.symphony.api.model.AppAuthenticateRequest;
import com.symphony.api.model.ExtensionAppTokens;
import com.symphony.api.pod.PodApi;
import com.symphony.id.SymphonyIdentity;
import com.symphony.spring.api.builders.ApiBuilderFactory;
import com.symphony.spring.api.properties.ProxyProperties;
import com.symphony.spring.app.SymphonyAppProperties;
import com.symphony.spring.app.pods.info.PodInfo;
import com.symphony.spring.app.pods.info.PodInfoStore;

/**
 * Tries returning tokens from pod details stored in the {@link PodInfoStore}.
 * 
 * @author Rob Moffat
 *
 */
public class PodInfoStoreTokenStrategy extends AbstractPodTokenStrategy<PodInfo> {

	PodInfoStore store;
	SymphonyAppProperties appProperties;
	
	public PodInfoStoreTokenStrategy(SymphonyAppProperties appProperties, SymphonyIdentity appIdentity,
			ApiBuilderFactory abf, TrustManager[] trustManagers, PodInfoStore store) {
		super(appIdentity, abf, trustManagers);
		this.store = store;
		this.appProperties = appProperties;
	}

	@Override
	protected PodInfo getPodProperties(String podId) {
		PodInfo pi = store.getPodInfo(podId);
		return pi;
	}

	@Override
	protected ExtensionAppTokens certBasedRequest(String appToken, PodInfo pod) throws Exception {
		ApiBuilder ab = setupApiBuilder(pod, pod.getPayload().getSessionAuthUrl());
		AuthenticationApi aa = ab.getApi(AuthenticationApi.class);
		AppAuthenticateRequest ar = new AppAuthenticateRequest();
		ar.setAppToken(appToken);
		ExtensionAppTokens out = aa.v1AuthenticateExtensionAppPost(ar);
		return out;

	}

	private ApiBuilder setupApiBuilder(PodInfo pod, String url) throws Exception {
		ConfigurableApiBuilder ab = abf.getObject();
		ab.setUrl(url);
		ab.setKeyManagers(appIdentity.getKeyManagers());
		ab.setTrustManagers(trustManagers);
		if (pod.getUseProxy() != Boolean.FALSE) {
			ProxyProperties proxy = appProperties.getProxy();
			if (proxy != null) {
				proxy.configure(ab);
			}
		}
		return ab;
	}

	@Override
	protected PodApi getPodApi(Map<String, Object> claims) throws Exception {
		String companyId = getCompanyId(claims);
		PodInfo pi = getPodProperties(companyId);
		if (pi == null) {
			return null;
		}
		
		ApiBuilder ab =  setupApiBuilder(pi, pi.getPayload().getPodUrl());
		return ab.getApi(PodApi.class);	}

}
