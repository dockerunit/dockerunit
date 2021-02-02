package com.github.dockerunit.discovery.consul;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerunit.discovery.consul.ServiceRecord.Check;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ConsulHttpResolver {

	private final HttpClient httpClient;
	private final String host;
	private final int port;
	private final ObjectMapper mapper = new ObjectMapper();
	
	public ConsulHttpResolver(String host, int port) {
		this.host = host;
		this.port = port;
		httpClient = HttpClientBuilder.create().build();
	}

	public Void verifyCleanup(String serviceName, int expectedRecords, int timeoutInSeconds, int frequencyInSeconds) {
		BiConsumer<CompletableFuture<Void>, Throwable> errorConsumer = (fut, t) -> {
				fut.complete(null);
		};
		BiConsumer<CompletableFuture<Void>, List<ServiceRecord>> matchingConsumer = (fut, records) -> fut.complete(null);
		return performQuerying(serviceName, expectedRecords, timeoutInSeconds, frequencyInSeconds, errorConsumer,
				matchingConsumer);
	}

	public List<ServiceRecord> resolveService(String serviceName, int expectedRecords, int timeoutInSeconds,
			int frequencyInSeconds, int initialDelayInSeconds) {

	    CompletableFuture<List<ServiceRecord>> result = new CompletableFuture<>();
	    TimerTask discovery = new TimerTask() {
			@Override
			public void run() {
				BiConsumer<CompletableFuture<List<ServiceRecord>>, Throwable> errorConsumer = (fut, t) -> {
				};
				BiConsumer<CompletableFuture<List<ServiceRecord>>, List<ServiceRecord>> matchingConsumer = (fut, records) -> fut
						.complete(records);
				try {
					List<ServiceRecord> serviceRecords = performQuerying(serviceName, expectedRecords,
							timeoutInSeconds, frequencyInSeconds, errorConsumer, matchingConsumer);
					result.complete(serviceRecords);
				} catch (Exception e) {
					result.completeExceptionally(e.getCause());
				}
			}
		};

		Timer timer = new Timer();
		timer.schedule(discovery, initialDelayInSeconds * 1000);
		return result.join();
	}

	private <T> T performQuerying(String serviceName, int expectedRecords, int timeoutInSeconds, int pollingPeriodInSeconds,
			BiConsumer<CompletableFuture<T>, Throwable> errorConsumer,
			BiConsumer<CompletableFuture<T>, List<ServiceRecord>> matchingConsumer) {
		CompletableFuture<T> result = new CompletableFuture<>();
		final AtomicInteger counter = new AtomicInteger(0);

		TimerTask repeatedTask = new TimerTask() {
			public void run() {
				List<ServiceRecord> records = null;
				try {
					records = getHealthyRecords(serviceName);
				} catch (Exception e) {
					result.completeExceptionally(e);
				}
				int counterValue = counter.incrementAndGet();
				if (records != null && records.size() == expectedRecords) {
					this.cancel();
					matchingConsumer.accept(result, records);
				} else {
					if (timedout(timeoutInSeconds, pollingPeriodInSeconds, counterValue)) {
						this.cancel();
						result.completeExceptionally(new RuntimeException("Discovery timed out."));
					}
				}
			}
		};

		Timer timer = new Timer("consul-polling-" + serviceName);
		timer.scheduleAtFixedRate(repeatedTask, 0, pollingPeriodInSeconds * 1000);

		result.exceptionally(ex -> {
			throw new RuntimeException("Discovery/cleanup failed for svc " + serviceName);
		});
		return result.join();
	}

	private List<ServiceRecord> getHealthyRecords(String serviceName) throws IOException, ClientProtocolException {
		List<ServiceRecord> allRecords = getCatalog(serviceName);
		List<ServiceRecord> unhealthy = getUnhealthy(serviceName);
		return allRecords.stream()
				.filter(r -> {
					return unhealthy.stream()
						.filter(uh -> uh.getPort() == r.getPort())
						.collect(Collectors.toList()).size() == 0;						
				}).collect(Collectors.toList());
	}

	private List<ServiceRecord> getUnhealthy(String serviceName) throws ClientProtocolException, IOException {
		List<ServiceRecord> records;
		HttpResponse response = null;
		HttpGet get = new HttpGet("http://" + host + ":" + port + "/v1/health/service/" + serviceName);
		response = httpClient.execute(get);
		records = parseUnhealthy(response);
		return records;
	}

	private List<ServiceRecord> getCatalog(String serviceName) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		HttpGet get = new HttpGet("http://" + host + ":" + port + "/v1/catalog/service/" + serviceName);
		response = httpClient.execute(get);
		return mapper.reader().forType(new TypeReference<List<ServiceRecord>>() {
		}).readValue(response.getEntity().getContent());
	}

	private List<ServiceRecord> parseUnhealthy(HttpResponse response) throws UnsupportedOperationException, IOException {
		List<ServiceRecord> records = mapper.reader().forType(new TypeReference<List<ServiceRecord>>() {
		}).readValue(response.getEntity().getContent());
		if(records != null) {
			records = records.stream()
					.filter(r -> {
						List<Check> failingChecks = new ArrayList<>();
						if(r.getChecks() != null) {
							failingChecks = r.getChecks().stream()
								.filter(c -> !c.getStatus().equalsIgnoreCase(Check.PASSING))
								.collect(Collectors.toList());
						}
						return failingChecks.size() > 0;
					}).collect(Collectors.toList());
		}
		return records;
	}
	
	private boolean timedout(int timeoutInSeconds, int frequencyInSeconds, int counterValue) {
		return counterValue * frequencyInSeconds >= timeoutInSeconds;
	}
}
