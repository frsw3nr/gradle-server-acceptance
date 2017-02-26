select node_name, domain_name, metric_name, value
from nodes,test_results,metrics,domains
where test_results.node_id = nodes.id
and test_results.metric_id = metrics.id
and metrics.domain_id = domains.id
and node_name = 'w2016'
order by metrics.id
