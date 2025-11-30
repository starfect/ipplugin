package me.starfect.ipplugin.data;

import java.util.Objects;
import java.util.Optional;

public final class IpInfo {
    private final String ip;
    private final String country;
    private final String countryCode;
    private final String region;
    private final String city;
    private final String org;
    private final String isp;
    private final String timezone;
    private final Double latitude;
    private final Double longitude;
    private final String provider;

    private IpInfo(Builder builder) {
        this.ip = builder.ip;
        this.country = builder.country;
        this.countryCode = builder.countryCode;
        this.region = builder.region;
        this.city = builder.city;
        this.org = builder.org;
        this.isp = builder.isp;
        this.timezone = builder.timezone;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.provider = builder.provider;
    }

    public String ip() {
        return ip;
    }

    public String country() {
        return country;
    }

    public String countryCode() {
        return countryCode;
    }

    public String region() {
        return region;
    }

    public String city() {
        return city;
    }

    public String org() {
        return org;
    }

    public String isp() {
        return isp;
    }

    public String timezone() {
        return timezone;
    }

    public Optional<Double> latitude() {
        return Optional.ofNullable(latitude);
    }

    public Optional<Double> longitude() {
        return Optional.ofNullable(longitude);
    }

    public String provider() {
        return provider;
    }

    @Override
    public String toString() {
        return "IpInfo{" +
                "ip='" + ip + '\'' +
                ", country='" + country + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", region='" + region + '\'' +
                ", city='" + city + '\'' +
                ", org='" + org + '\'' +
                ", isp='" + isp + '\'' +
                ", timezone='" + timezone + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", provider='" + provider + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IpInfo ipInfo = (IpInfo) o;
        return Objects.equals(ip, ipInfo.ip) &&
                Objects.equals(country, ipInfo.country) &&
                Objects.equals(countryCode, ipInfo.countryCode) &&
                Objects.equals(region, ipInfo.region) &&
                Objects.equals(city, ipInfo.city) &&
                Objects.equals(org, ipInfo.org) &&
                Objects.equals(isp, ipInfo.isp) &&
                Objects.equals(timezone, ipInfo.timezone) &&
                Objects.equals(latitude, ipInfo.latitude) &&
                Objects.equals(longitude, ipInfo.longitude) &&
                Objects.equals(provider, ipInfo.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, country, countryCode, region, city, org, isp, timezone, latitude, longitude, provider);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String ip;
        private String country;
        private String countryCode;
        private String region;
        private String city;
        private String org;
        private String isp;
        private String timezone;
        private Double latitude;
        private Double longitude;
        private String provider;

        private Builder() {
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder org(String org) {
            this.org = org;
            return this;
        }

        public Builder isp(String isp) {
            this.isp = isp;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public IpInfo build() {
            return new IpInfo(this);
        }
    }
}
