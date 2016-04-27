package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.auth.UserAndPasswordAuthentication;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.Extension;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang.StringUtils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 *  https://github.com/helm/charts
 *
 *  https://git.elasticbox.com/api/v3/
 *  https://api.github.com/
 *  https://raw.githubusercontent.com/
 *
 *
 * https://git.elasticbox.com/raw/serna/jenkins-plugin-kubernetes/master/pom.xml?token=AAAAL7xdKEfxQtVtXnmabhzDNdA-d-rrks5XHzQIwA%3D%3D
 * https://raw.githubusercontent.com/helm/charts/master/rabbitmq/Chart.yaml
 *
 * https://git.elasticbox.com/api/v3/repos/serna/jenkins-plugin-kubernetes/contents
 * https://api.github.com/repos/helm/charts/contents
 **
            GitHubApiContentsService githubService = new Retrofit.Builder()
                             .baseUrl("https://api.github.com")
                             .addConverterFactory(GsonConverterFactory.create())
                             .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                             .build()
                             .create(GitHubApiContentsService.class);

 BASE URL
    api.github.com
    my.domain.com


 *
 */
@Extension
public class GitHubClientsFactoryImpl implements GitHubClientsFactory {

    Map<String, GitHubClient> clients = new HashMap<String, GitHubClient>();

    private static GitHubClientsFactoryPartBuilder [] partBuilders = new GitHubClientsFactoryPartBuilder[] {
        new AddBaseUrl(),
        new AddResponseConverters(),
        new AddAuthenticationTokenInterceptor(),
        new AddClientAndPasswordAuthenticationInterceptor(),
        new AddLoggingInterceptor()
    };

    private static class ClientsFactoryBuilderContext {

        private String apiBaseUrl;
        private GitHubApiResponseContentType responseContentType;
        private Authentication authentication;

        private Retrofit.Builder clientBuilder = new Retrofit.Builder();
        private OkHttpClient.Builder okHttpClientBuilder;

        private boolean atLeastOneAuthenticationMethodProvided = false;
        private boolean debug = true;

        public ClientsFactoryBuilderContext(String apiBaseUrl, GitHubApiResponseContentType responseContentType) {
            this.apiBaseUrl = apiBaseUrl;
            this.responseContentType = responseContentType;
        }

        public ClientsFactoryBuilderContext(String apiBaseUrl, GitHubApiResponseContentType responseContentType,
                                            Authentication authentication) {
            this.apiBaseUrl = apiBaseUrl;
            this.responseContentType = responseContentType;
        }

        public Authentication getAuthentication() {
            return authentication;
        }

        public GitHubApiResponseContentType getResponseContentType() {
            return responseContentType;
        }

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public OkHttpClient.Builder getOkHttpClientBuilder() {
            return okHttpClientBuilder;
        }

        public void setOkHttpClientBuilder(OkHttpClient.Builder okHttpClientBuilder) {
            this.okHttpClientBuilder = okHttpClientBuilder;
        }

        public Retrofit.Builder getClientBuilder() {
            return clientBuilder;
        }

        public void setClientBuilder(Retrofit.Builder clientBuilder) {
            this.clientBuilder = clientBuilder;
        }

        public boolean isAtLeastOneAuthenticationMethodProvided() {
            return atLeastOneAuthenticationMethodProvided;
        }

        public void setAtLeastOneAuthenticationMethodProvided(boolean atLeastOneAuthenticationMethodProvided) {
            this.atLeastOneAuthenticationMethodProvided = atLeastOneAuthenticationMethodProvided;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }
    }

    public interface GitHubClientsFactoryPartBuilder {

        void buildPart(ClientsFactoryBuilderContext context);

    }

    public static class AddBaseUrl implements GitHubClientsFactoryPartBuilder {

        @Override
        public void buildPart(ClientsFactoryBuilderContext context) {
            context.getClientBuilder().baseUrl(context.getApiBaseUrl());
        }
    }

    public static class AddResponseConverters implements GitHubClientsFactoryPartBuilder {

        @Override
        public void buildPart(ClientsFactoryBuilderContext context) {
            final GitHubApiResponseContentType responseContentType = context.getResponseContentType();

            switch (responseContentType) {
                case JSON:
                    context.getClientBuilder().addConverterFactory(GsonConverterFactory.create());
                    break;

                case RAW_STRING:
                    context.getClientBuilder().addConverterFactory(ScalarsConverterFactory.create());
                    break;

                default:
            }
        }
    }


    public static class AddAuthenticationTokenInterceptor implements GitHubClientsFactoryPartBuilder {
        @Override
        public void buildPart(ClientsFactoryBuilderContext context) {
            //TODO not implemented yet
        }
    }


    public static class AddLoggingInterceptor implements GitHubClientsFactoryPartBuilder {

        @Override
        public void buildPart(ClientsFactoryBuilderContext context) {

            if (context.isDebug()) {

                if (context.getOkHttpClientBuilder() == null) {
                    final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                    context.setOkHttpClientBuilder(httpClient);
                }

                final OkHttpClient.Builder okHttpClientBuilder = context.getOkHttpClientBuilder();

                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

                // set your desired log level
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                okHttpClientBuilder.addInterceptor(logging);
            }

        }
    }



    public static class AddClientAndPasswordAuthenticationInterceptor implements GitHubClientsFactoryPartBuilder {

        public static final String BASIC_AUTH_TOKEN = "Basic";
        public static final String BASIC_AUTH_HEADER = "Authorization";

        @Override
        public void buildPart(ClientsFactoryBuilderContext context) {

            if (context.isAtLeastOneAuthenticationMethodProvided()) {
                return;
            }

            if (context.getAuthentication() != null) {

                final Authentication authentication = context.getAuthentication();
                if (authentication instanceof UserAndPasswordAuthentication) {

                    UserAndPasswordAuthentication authenticationMethod = (UserAndPasswordAuthentication) authentication;

                    String username = authenticationMethod.getUser();
                    String password = authenticationMethod.getPassword();

                    if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {

                        String credentials = username + ":" + password;
                        final String basic = BASIC_AUTH_TOKEN + " " + new BASE64Encoder().encode(credentials.getBytes());

                        final OkHttpClient.Builder okBuilder = new OkHttpClient.Builder()
                            .addInterceptor(new Interceptor() {
                                @Override
                                public Response intercept(Chain chain) throws IOException {
                                    final Request request = chain.request()
                                        .newBuilder()
                                        .addHeader(BASIC_AUTH_HEADER, basic)
                                        .build();

                                    final Response response = chain.proceed(request);
                                    return response;
                                }
                            });

                        context.setOkHttpClientBuilder(okBuilder);

                        context.setAtLeastOneAuthenticationMethodProvided(true);
                    }
                }

            }
        }
    }

    @Override
    public <T> T getClient(String baseUrl,
                           Authentication authentication,
                           Class<T> serviceTypeInterface,
                           GitHubApiResponseContentType responseType) throws RepositoryException {

        final String apiBaseUrl = GitHubApiType.findOrComposeApiBaseUrl(baseUrl);

        if (clients.containsKey(apiBaseUrl)) {
            return (T) clients.get(apiBaseUrl).getClient();
        }

        ClientsFactoryBuilderContext context = new ClientsFactoryBuilderContext(apiBaseUrl, responseType, authentication);

        return getClient(serviceTypeInterface, responseType, apiBaseUrl);
    }

    @Override
    public <T> T getClient(String baseUrl, Class<T> serviceTypeInterface, GitHubApiResponseContentType responseType)
        throws RepositoryException {

        final String apiBaseUrl = GitHubApiType.findOrComposeApiBaseUrl(baseUrl);

        if (clients.containsKey(apiBaseUrl)) {
            return (T) clients.get(apiBaseUrl).getClient();
        }

        return getClient(serviceTypeInterface, responseType, apiBaseUrl);
    }

    private <T> T getClient(Class<T> serviceTypeInterface,
                            GitHubApiResponseContentType responseType,
                            String apiBaseUrl) {

        ClientsFactoryBuilderContext context = new ClientsFactoryBuilderContext(apiBaseUrl, responseType);

        for (GitHubClientsFactoryPartBuilder partBuilder : partBuilders) {
            partBuilder.buildPart(context);
        }

        final Retrofit.Builder clientBuilder = context.getClientBuilder();

        if (context.getOkHttpClientBuilder() != null) {
            final OkHttpClient.Builder okHttpClientBuilder = context.getOkHttpClientBuilder();
            final OkHttpClient okHttpClient = okHttpClientBuilder.build();
            clientBuilder.client(okHttpClient);
        }

        final T service = clientBuilder
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()
            .create(serviceTypeInterface);

        if (service != null) {
            clients.put(apiBaseUrl, new GitHubClient<>(apiBaseUrl, service));
        }

        return service;
    }


}
