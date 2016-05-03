package com.elasticbox.jenkins.k8s.repositories.api.charts.factory;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.ScalarEvent;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;

/**
 * Created by serna on 4/19/16.
 */
public class ManifestFactory {


    public static void addManifest(String yamlAsText, Chart.ChartBuilder chartBuilder) throws RepositoryException {

        final String manifestKind = findManifestKind(yamlAsText);

        final ManifestType type = ManifestType.findByType(manifestKind);

        KubernetesClient client = new DefaultKubernetesClient();

        final InputStream inputStream = IOUtils.toInputStream(yamlAsText);

        switch (type) {

            case POD:
                final Pod pod = client.pods().load(inputStream).get();
                chartBuilder.addPod(pod);
                return;

            case REPLICATION_CONTROLLER:
                final ReplicationController replicationController =
                        client.replicationControllers().load(inputStream).get();
                chartBuilder.addReplicationController(replicationController);
                return;

            case SERVICE:
                final Service service = client.services().load(inputStream).get();
                chartBuilder.addService(service);
                return;

            default:

        }

        throw new RepositoryException("Manifest kind: " + manifestKind + " is not supported");
    }

    private static String findManifestKind(String yamlAsText) {

        final Yaml yaml = new Yaml();
        final StringReader stringReader = new StringReader(yamlAsText);
        final Iterable<Event> parse = yaml.parse(stringReader);
        boolean kindFound = false;
        int countBetween = 0;
        String kind = null;
        for (Event event : parse) {
            if (kindFound) {
                countBetween++;
            }
            if (event.is(Event.ID.Scalar)) {
                ScalarEvent scalarEvent = (ScalarEvent)event;
                final String value = scalarEvent.getValue();
                if (countBetween == 1) {
                    kind = value;
                    break;
                }
                if (value.equals("kind")) {
                    kindFound = true;
                    continue;
                }

            }
        }

        return kind;
    }
}
