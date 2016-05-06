package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.auth.TokenAuthentication;
import com.elasticbox.jenkins.k8s.auth.UserAndPasswordAuthentication;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.Extension;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


@Extension
public class GitHubClientsFactoryImpl implements GitHubClientsFactory {

    private static final Logger LOGGER = Logger.getLogger(GitHubClientsFactoryImpl.class.getName() );

    public static final int MAX_NUM_GITHUB_CLIENTS_CACHED = 100;
    public static final int CACHED_HOURS = 24;


    private LoadingCache<ClientsFactoryBuilderContext, GitHubClient> cache = CacheBuilder.newBuilder()
        .maximumSize(MAX_NUM_GITHUB_CLIENTS_CACHED)
        .expireAfterAccess(CACHED_HOURS, TimeUnit.HOURS)
        .build(new GithubClientCacheLoader());


    private static GitHubClientsFactoryPartBuilder [] partBuilders = new GitHubClientsFactoryPartBuilder[] {
        new AddBaseUrl(),
        new AddResponseConverters(),
        new AddAuthenticationTokenInterceptor(),
        new AddClientAndPasswordAuthenticationInterceptor(),
        new AddLoggingInterceptor()
    };

    private static class ClientsFactoryBuilderContext<T> {

        private Class<T> serviceTypeInterfaceClass;
        private String apiBaseUrl;
        private GitHubApiResponseContentType responseContentType;
        private Authentication authentication;
        private boolean debug = false;


        private Retrofit.Builder clientBuilder = new Retrofit.Builder();
        private OkHttpClient.Builder okHttpClientBuilder;
        private boolean atLeastOneAuthenticationMethodProvided = false;

        public ClientsFactoryBuilderContext(String apiBaseUrl,
                                            Class<T> serviceTypeInterfaceClass,
                                            GitHubApiResponseContentType responseContentType) {
            this.apiBaseUrl = apiBaseUrl;
            this.serviceTypeInterfaceClass = serviceTypeInterfaceClass;
            this.responseContentType = responseContentType;
        }

        public ClientsFactoryBuilderContext(String apiBaseUrl,
                                            Class<T> serviceTypeInterfaceClass,
                                            GitHubApiResponseContentType responseContentType,
                                            Authentication authentication) {
            this.apiBaseUrl = apiBaseUrl;
            this.serviceTypeInterfaceClass = serviceTypeInterfaceClass;
            this.responseContentType = responseContentType;
            this.authentication = authentication;
        }

        public Authentication getAuthentication() {
            return authentication;
        }

        public Class<T> getServiceTypeInterfaceClass() {
            return serviceTypeInterfaceClass;
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

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            ClientsFactoryBuilderContext<?> that = (ClientsFactoryBuilderContext<?>) object;

            if (debug != that.debug) {
                return false;
            }
            if (serviceTypeInterfaceClass != null ? !serviceTypeInterfaceClass.equals(that.serviceTypeInterfaceClass)
                : that.serviceTypeInterfaceClass != null) {
                return false;
            }
            if (apiBaseUrl != null ? !apiBaseUrl.equals(that.apiBaseUrl) : that.apiBaseUrl != null) {
                return false;
            }
            if (responseContentType != that.responseContentType) {
                return false;
            }
            return !(authentication != null
                ? !authentication.equals(that.authentication)
                : that.authentication != null);

        }

        @Override
        public int hashCode() {
            int result = serviceTypeInterfaceClass != null ? serviceTypeInterfaceClass.hashCode() : 0;
            result = 31 * result + (apiBaseUrl != null ? apiBaseUrl.hashCode() : 0);
            result = 31 * result + (responseContentType != null ? responseContentType.hashCode() : 0);
            result = 31 * result + (authentication != null ? authentication.hashCode() : 0);
            result = 31 * result + (debug ? 1 : 0);
            return result;
        }
    }

    public interface GitHubClientsFactoryPartBuilder {

        void buildPart(ClientsFactoryBuilderContext context);

    }

    private static class AddBaseUrl implements GitHubClientsFactoryPartBuilder {

        @Override
        public void buildPart(ClientsFactoryBuilderContext context) {
            final String apiBaseUrl = context.getApiBaseUrl();
            context.getClientBuilder().baseUrl(apiBaseUrl);
        }
    }

    private static class AddResponseConverters implements GitHubClientsFactoryPartBuilder {

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


    private static class AddAuthenticationTokenInterceptor implements GitHubClientsFactoryPartBuilder {

        public static final String TOKEN = "token";
        public static final String BASIC_AUTH_HEADER = "Authorization";

        @Override
        public void buildPart(ClientsFactoryBuilderContext context) {

            if (context.isAtLeastOneAuthenticationMethodProvided()) {
                return;
            }

            if (context.getAuthentication() != null) {

                final Authentication authentication = context.getAuthentication();
                if (authentication instanceof TokenAuthentication) {

                    TokenAuthentication authenticationMethod = (TokenAuthentication) authentication;

                    String token = authenticationMethod.getAuthToken();

                    if (StringUtils.isNotBlank(token)) {

                        final String basic = TOKEN + " " + token;

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


    private static class AddLoggingInterceptor implements GitHubClientsFactoryPartBuilder {

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



    private static class AddClientAndPasswordAuthenticationInterceptor implements GitHubClientsFactoryPartBuilder {

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

                    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {

                        final String credentials = username + ":" + password;
                        final String basic =
                            BASIC_AUTH_TOKEN + " " + new Base64()
                                .encodeToString(credentials.getBytes(Charset.forName("UTF-8")));

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


    private static class GithubClientCacheLoader extends CacheLoader<ClientsFactoryBuilderContext, GitHubClient> {

        @Override
        public GitHubClient load(ClientsFactoryBuilderContext context) throws Exception {

            for (GitHubClientsFactoryPartBuilder partBuilder : partBuilders) {
                partBuilder.buildPart(context);
            }

            Retrofit.Builder clientBuilder = context.getClientBuilder();

            if (context.getOkHttpClientBuilder() != null) {
                final OkHttpClient.Builder okHttpClientBuilder = context.getOkHttpClientBuilder();
                final OkHttpClient okHttpClient = okHttpClientBuilder.build();
                clientBuilder = clientBuilder.client(okHttpClient);
            }


            return new GitHubClient<>(context.getApiBaseUrl(), clientBuilder
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(context.getServiceTypeInterfaceClass()));

        }
    }


    @Override
    public <T> T getClient(String baseUrl,
                       Authentication authentication,
                       Class<T> serviceTypeInterface,
                       GitHubApiResponseContentType responseType) throws RepositoryException {

        final String apiBaseUrl = GitHubApiType.findOrComposeApiBaseUrl(baseUrl);

        ClientsFactoryBuilderContext<T> context =
            new ClientsFactoryBuilderContext<>(apiBaseUrl, serviceTypeInterface, responseType, authentication);


        final GitHubClient<T> client;
        try {
            client = cache.get(context);
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Error getting the GitHub client object", e);
            throw new RepositoryException(e);
        }

        return client.getClient();

    }

    @Override
    public <T> T getClient(String baseUrl, Class<T> serviceTypeInterface, GitHubApiResponseContentType responseType)
        throws RepositoryException {

        final String apiBaseUrl = GitHubApiType.findOrComposeApiBaseUrl(baseUrl);

        ClientsFactoryBuilderContext<T> context =
            new ClientsFactoryBuilderContext<>(apiBaseUrl, serviceTypeInterface, responseType);


        final GitHubClient<T> client;
        try {
            client = cache.get(context);
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Error getting the GitHub client object", e);
            throw new RepositoryException(e);
        }

        return client.getClient();

    }

    LoadingCache<ClientsFactoryBuilderContext, GitHubClient> getCache() {
        return cache;
    }
}
