package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import java.text.MessageFormat;
import java.util.EnumSet;

/**
 * Base URL posibilities:
 *
 * https://api.github.com
 * https://raw.githubusercontent.com
 * http(s)://hostname/api/v3/
 */
public enum GitHubApiType implements GitHubApi {

    PUBLIC_API {

        @Override
        public boolean isApplicableFor(GitHubUrl url) {
            if (getUrlType().endsWith(url.host() + "/")) {
                return true;
            }
            return false;
        }

        @Override
        public String getUrlType() {
            return "https://api.github.com/";
        }
    },
    PUBLIC_RAW_CONTENT {

        @Override
        public boolean isApplicableFor(GitHubUrl url) {
            if (getUrlType().equals(url.protocol() + "://" + url.host() + "/")) {
                return true;
            }
            return false;
        }

        @Override
        public String getUrlType() {
            return "https://raw.githubusercontent.com/";
        }
    },
    ENTERPRISE_RAW {

        @Override
        public boolean isApplicableFor(GitHubUrl url) {
            final String[] pathTokens = url.pathAsArray();
            return pathTokens[0].equals("raw");
        }

        @Override
        public String getUrlType() {

            return "{0}raw/";
        }
    },
    ENTERPRISE {

        @Override
        public boolean isApplicableFor(GitHubUrl url) {
            return false;
        }

        @Override
        public String getUrlType() {
            return "{0}api/v3/";
        }
    };


    public static String findOrComposeApiBaseUrl(String url) {

        GitHubUrl gitHubUrl = new GitHubUrl(url);
        final GitHubApiType apiType = findBy(gitHubUrl);

        return findOrComposeApiBaseUrl(apiType, gitHubUrl);
    }


    private static String findOrComposeApiBaseUrl(GitHubApiType gitHubApiType, GitHubUrl url) {
        switch (gitHubApiType) {
            case PUBLIC_API:
                return PUBLIC_API.getUrlType();

            case PUBLIC_RAW_CONTENT:
                return PUBLIC_RAW_CONTENT.getUrlType();

            case ENTERPRISE_RAW:
                return MessageFormat.format(ENTERPRISE_RAW.getUrlType(), url.getHostAndPortTogether());

            default:
                return MessageFormat.format(ENTERPRISE.getUrlType(), url.getHostAndPortTogether());
        }
    }

    public static GitHubApiType findBy(GitHubUrl url) {
        final EnumSet<GitHubApiType> apiSet = EnumSet.of(PUBLIC_API, PUBLIC_RAW_CONTENT, ENTERPRISE_RAW, ENTERPRISE);
        for (GitHubApiType gitHubApiType : apiSet) {
            if (gitHubApiType.isApplicableFor(url)) {
                return gitHubApiType;
            }
        }
        return ENTERPRISE;
    }
}
