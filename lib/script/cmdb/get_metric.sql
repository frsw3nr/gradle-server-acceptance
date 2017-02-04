select node_name, domain_name, metric_name, value 
from node,test_result,metric,domain
where test_result.node_id = node.id
and test_result.metric_id = metric.id
and metric.domain_id = domain.id
and node_name = 'ostrich'
order by metric.id



