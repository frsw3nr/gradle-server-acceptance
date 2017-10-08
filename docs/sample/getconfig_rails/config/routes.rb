Rails.application.routes.draw do
  resources :nodes
  get 'node/index'

  get 'test_result/index'

  get 'inventory/index'
  get 'device_inventory/index'

  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
end
